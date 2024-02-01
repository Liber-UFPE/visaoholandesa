package br.ufpe.liber.tasks

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

@Suppress("detekt:MagicNumber")
object DatabaseBooks : LongIdTable("ne_book", "book_id") {
    val title = varchar("title", 255)
    val alternative = varchar("alternative", 255).nullable()
    val creator = varchar("creator", 255)
    val publisher = varchar("publisher", 255)
    val date = varchar("date", 255).nullable()
    val local = varchar("local", 255)
    val collection = varchar("collection", 255)
    val language = varchar("language", 255)
    val contributor = varchar("contributor", 255)
    val subject = varchar("subject", 255).nullable()
    val description = text("description").nullable()
    val rights = varchar("rights", 255).nullable()

    // Uses `theSource` to avoid conflict with superclass
    val theSource = varchar("source", 255).nullable()
    val text = varchar("text", 255)
}

class DatabaseBook(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DatabaseBook>(DatabaseBooks)

    val title by DatabaseBooks.title
    val alternative by DatabaseBooks.alternative
    val creator by DatabaseBooks.creator
    val publisher by DatabaseBooks.publisher
    val date by DatabaseBooks.date
    val local by DatabaseBooks.local
    val collection by DatabaseBooks.collection
    val language by DatabaseBooks.language
    val contributor by DatabaseBooks.contributor
    val subject by DatabaseBooks.subject
    val description by DatabaseBooks.description
    val rights by DatabaseBooks.rights
    val theSource by DatabaseBooks.theSource
    val text by DatabaseBooks.text
    val pages by DatabasePage referrersOn DatabasePages.bookId
}

object DatabasePages : LongIdTable("ne_page", "page_id") {
    val text = text("text")
    val number = long("number")
    val bookId = reference("fk_book_id", DatabaseBooks)
}

class DatabasePage(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DatabasePage>(DatabasePages)

    val number by DatabasePages.number
    val book by DatabaseBook referencedOn DatabasePages.bookId
    private val text by DatabasePages.text
    private val notes by DatabaseNote referrersOn DatabaseNotes.pageId

    fun toMarkdown(): String =
        processPageText(text) + "\n\n" + notes.joinToString(separator = "\n\n") { it.toMarkdown().trim() }

    private fun processPageText(text: String): String {
        return text.replace("</strong><strong>", "</strong><br /></strong>")
            // Removes break lines at the beginning or end of the content
            .replace("(^<br />|<br />(\\s|\\p{Punct})?\$)".toRegex(), "")
            // Replaces "<strong>Something</strong>" at the beginning of the text with "## Something\n\n"
            // since this would be the first "title" for the content.
            // Using "##" (h2) because the page where this would be rendered most likely will already have a h1.
            .replace("^<strong>([^<]+)</strong>".toRegex()) { matchResult ->
                "## ${matchResult.groupValues[1]}\n\n"
            }
            // Replaces HTML breaks with regular string line breaks
            .replace("<br />", "\n")
            // Replaces ?Something? with "Something". This was some problem when OCRing the text
            .replace("\\?([^?]+)\\?".toRegex()) { matchResult ->
                "\"${matchResult.groupValues[1]}\""
            }
            // Replaces "[nota 123]" with "[^nota-123]" with is compatible with Markdown footnotes.
            // See https://githubook.com/vsch/flexmark-java/wiki/Footnotes-Extension.
            .replace("\\[nota (\\d+)]".toRegex()) { result ->
                "[^nota-${result.groupValues[1]}]"
            }
            // Replaces [123] with [^nota-123] to normalize footnotes format.
            .replace("\\[(\\d+)]".toRegex()) { matchResult ->
                "[^nota-${matchResult.groupValues[1]}]"
            }
            // Replaces "Some text.[^nota-123]More text" with "Some text.[^nota-123] More text".
            // Attention to the extra space after the note.
            .replace("\\[\\^nota-(\\d+)](\\p{Upper})".toRegex()) { result ->
                "[^nota-${result.groupValues[1]}] ${result.groupValues[2]}"
            }
            .lines()
            .filter { it.isNotBlank() }
            .map { line ->
                // There are many lines that are all uppercase, and looks like titles.
                val trimmedLine = line.trim()
                when {
                    // We want to discard lines starting with "#" because they are already formatted as titles.
                    trimmedLine.startsWith("#") -> trimmedLine
                    // \p{Lu} means uppercase unicode letters: https://www.regular-expressions.info/unicode.html#category
                    trimmedLine.matches("^[\\p{Lu}\\p{Punct}\\s]+\$".toRegex()) -> "### $trimmedLine"
                    else -> trimmedLine
                }
            }
            .joinToString(separator = "\n\n") { line ->
                // Replaces "<strong>Something</strong>" at the beginning of the text with "### Something\n\n"
                // Using "###" (h3) since we may already have h2 for the page.
                // If not, using h3 is not a big deal.
                line.replace("^<strong>([^<]+)</strong>".toRegex()) { "### ${it.groupValues[1]}\n\n" }
                    // Some basic trim sanitization. :-)
                    .trim()
            }
    }
}

object DatabaseNotes : LongIdTable("ne_note", "note_id") {
    val number = integer("number")
    val text = text("text")
    val pageId = reference("fk_page_id", DatabasePages)
}

class DatabaseNote(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DatabaseNote>(DatabaseNotes)

    val page by DatabasePage referencedOn DatabaseNotes.pageId
    private val number by DatabaseNotes.number
    private val text by DatabaseNotes.text

    fun toMarkdown() = "[^nota-$number]: **Nota $number:** ${processNotesText(text)}"

    private fun processNotesText(text: String): String {
        return text.replace("</strong><strong>", "</strong><br /></strong>")
            // Removes break lines at the beginning or end of the content
            .replace("(^<br />|<br />(\\s|\\p{Punct})?\$)".toRegex(), "")
            // Removes "[123]" or "[nota 123]" at the begging of the notes to later normalize the format.
            .replace("^\\[(nota )?\\d+]".toRegex(), "")
            // Replaces HTML breaks with regular string line breaks
            .replace("<br />", "\n")
            // Replaces ?Something? with "Something". This was some problem when OCRing the text
            .replace("\\?([^?]+)\\?".toRegex()) { matchResult ->
                "\"${matchResult.groupValues[1]}\""
            }
            // Replaces "[i]Something[/i]" with "*Something*". See https://www.markdownguide.org/basic-syntax/#italic
            .replace("\\[i]([^\\[]+)\\[/i]".toRegex()) { matchResult ->
                "_${matchResult.groupValues[1]}_"
            }
            // Notes later won't have multiple line breaks, so let's use hr to split notes sections that are now
            // defined with repeated "-".
            .replace("-{3,}".toRegex(), "<hr />")
            .lines()
            .filter { it.isNotBlank() }
            .joinToString("\n") { it.trim() }
            .replace("\n<hr />\n".toRegex(), "<hr />")
    }
}

@CacheableTask
abstract class GenerateBooksJsonTask : DefaultTask() {

    @get:OutputDirectory
    abstract val booksOutputDirectory: DirectoryProperty

    init {
        // Generates directly into src folder because this is something we can commit.
        // The task exists so that we can have a repeatable process we can improve.
        booksOutputDirectory.convention(project.layout.projectDirectory.dir("resources/main/data/json"))
    }

    @TaskAction
    fun generateBooksJson() {
        Database.connect(
            "jdbc:mysql://localhost:3306/visao_holandesa",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "",
        )
        transaction {
            val prettyJson = Json {
                prettyPrint = true
            }

            addLogger(StdOutSqlLogger)
            DatabaseBook.all().map { book ->
                File("src/main/resources/data/json/book-${book.id.value}.json")
                    .writeText(
                        prettyJson.encodeToString(
                            mapOf(
                                "id" to book.id.value.toJson(),
                                "title" to book.title.toJson(),
                                "alternative" to book.alternative.safeToJson(),
                                "creator" to book.creator.toJson(),
                                "publisher" to book.publisher.toJson(),
                                "date" to book.date.safeToJson(),
                                "local" to book.local.toJson(),
                                "collection" to book.collection.toJson(),
                                "language" to book.language.toJson(),
                                "contributor" to book.contributor.toJson(),
                                "subject" to book.subject.safeToJson(),
                                "description" to book.description.safeToJson(),
                                "rights" to book.rights.safeToJson(),
                                "source" to book.theSource.safeToJson(),
                                "text" to book.text.toJson(),
                                "pages" to book.pages.map { page ->
                                    mapOf(
                                        "id" to page.id.value.toJson(),
                                        "text" to page.toMarkdown().toJson(),
                                        "number" to page.number.toJson(),
                                    ).toJson()
                                },
                            ),
                        ),
                    )
            }
        }
    }
}
