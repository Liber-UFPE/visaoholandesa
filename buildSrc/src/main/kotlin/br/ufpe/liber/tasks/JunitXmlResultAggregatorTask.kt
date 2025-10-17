package br.ufpe.liber.tasks

import org.apache.tools.ant.Project
import org.apache.tools.ant.taskdefs.optional.junit.XMLResultAggregator
import org.apache.tools.ant.types.FileSet
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.Serializable

@CacheableTask
abstract class JunitXmlResultAggregatorTask : DefaultTask() {

    @get:Input
    abstract val sources: Property<Sources>

    @get:OutputFile
    abstract val toFile: RegularFileProperty

    @get:OutputDirectory
    abstract val toDir: DirectoryProperty

    @TaskAction
    fun aggregateReports() {
        val aggregator = XMLResultAggregator()
        // Initialize task
        aggregator.project = ant.project
        aggregator.setTofile(toFile.get().asFile.name)
        aggregator.setTodir(toDir.get().asFile)
        aggregator.init()

        // Start a reporter
        val reporter = aggregator.createReport()
        reporter.setTodir(toDir.get().asFile)

        // Add filesets
        val fileSets = sources.get().toFileSets(ant.project)
        fileSets.forEach(aggregator::addFileSet)

        // go!
        aggregator.execute()
    }
}

data class Sources(val filesets: Map<File, String>) : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }

    @Suppress("detekt:SpreadOperator")
    constructor(vararg pairs: Pair<File, String>) : this(mapOf(*pairs))

    fun toFileSets(project: Project): List<FileSet> = filesets.map { (dir, includes) ->
        val fileSet = FileSet()
        fileSet.dir = dir
        fileSet.project = project
        fileSet.setIncludes(includes)
        fileSet
    }
}
