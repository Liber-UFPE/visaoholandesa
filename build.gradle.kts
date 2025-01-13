import br.ufpe.liber.tasks.GenerateAssetsMetadataTask
import com.adarshr.gradle.testlogger.theme.ThemeType
import com.lordcodes.turtle.shellRun
import java.lang.System.getenv

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.allopen") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.4.5"
    id("gg.jte.gradle") version "3.1.15"
    id("io.micronaut.aot") version "4.4.5"
    // Provides better test output
    id("com.adarshr.test-logger") version "4.0.0"
    // Code Coverage:
    // https://github.com/Kotlin/kotlinx-kover
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
    // Code Inspections
    // https://detekt.dev/
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
    // Easily add new test sets
    // https://github.com/unbroken-dome/gradle-testsets-plugin
    id("org.unbroken-dome.test-sets") version "4.1.0"
    // To manage docker images
    // https://github.com/bmuschko/gradle-docker-plugin
    id("com.bmuschko.docker-remote-api") version "9.4.0"
    // SonarQube/SonarCloud plugin
    // https://github.com/SonarSource/sonar-scanner-gradle
    id("org.sonarqube") version "6.0.1.5171"
    // Add diktat
    // https://github.com/marcospereira/diktat
    id("com.saveourtool.diktat") version "2.0.0"
    // To buil the app ui frontend
    // https://siouan.github.io/frontend-gradle-plugin/
    id("org.siouan.frontend-jdk17") version "10.0.0"
}

val runningOnCI: Boolean = getenv().getOrDefault("CI", "false").toBoolean()

val javaVersion: Int = 21

val kotlinVersion: String = properties["kotlinVersion"] as String
val micronautVersion: String = properties["micronautVersion"] as String
val jteVersion: String = properties["jteVersion"] as String
val luceneVersion: String = properties["luceneVersion"] as String
val flexmarkVersion: String = properties["flexmarkVersion"] as String

val releaseTag: String? by project
version = releaseTag ?: "0.1"
group = "br.ufpe.liber"

// Filter out generated files from source sets, and then filter in only existing files.
fun filterSourceSet(sourceSet: SourceSet): List<File> = sourceSet.allSource
    .srcDirs
    .filterNot {
        // Exclude generated files (inside build directory)
        it.absolutePath.startsWith(layout.buildDirectory.asFile.get().absolutePath)
    }
    .filter(File::exists)

val getNodeExecutable = try {
    Result.success(
        shellRun {
            files.which("node") ?: error("Node.js is not installed or not in the path.")
        },
    )
} catch (ex: IllegalStateException) {
    Result.failure<Exception>(ex)
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("br.ufpe.liber.Application")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

sonar {
    properties {
        property("sonar.projectKey", "Liber-UFPE_${project.name}")
        property("sonar.organization", "liber-ufpe")
        property("sonar.host.url", "https://sonarcloud.io")

        property("sonar.kotlin.file.suffixes", listOf(".kt", ".kts"))

        // https://docs.sonarsource.com/sonarcloud/enriching/test-coverage/test-coverage-parameters/#javakotlinscalajvm
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/kover/report.xml")

        // https://docs.sonarsource.com/sonarcloud/enriching/external-analyzer-reports/#kotlin
        property("sonar.kotlin.detekt.reportPaths", "build/reports/detekt/detekt.xml")

        // https://docs.sonarsource.com/sonarcloud/advanced-setup/languages/kotlin/#specifying-the-kotlin-source-code-version
        property("sonar.kotlin.source.version", "1.9")

        property("sonar.sources", filterSourceSet(sourceSets.main.get()))

        property(
            "sonar.tests",
            sourceSets
                // Include any source set that contains test in its name.
                // For example, "test", "integrationTest", etc.
                .filter { sourceSet -> sourceSet.name.contains("test", ignoreCase = true) }
                .flatMap(::filterSourceSet),
        )

        // See docs here:
        // https://docs.sonarsource.com/sonarqube/latest/project-administration/analysis-scope/#code-coverage-exclusion
        property(
            "sonar.coverage.exclusions",
            listOf("**/*Generated*"),
        )

        property(
            "sonar.exclusions",
            listOf("src/main/**/books/*.txt"),
        )
    }
}

diktat {
    inputs {
        include("src/**/*.kt")
        exclude("src/main/kotlin/br/ufpe/liber/tasks/**/*.kt")
        exclude("src/accessibilityTest/**/*.kt")
    }
}

frontend {
    nodeVersion = "20.15.0"
    // The plugin will NOT try to download Node.js.
    nodeDistributionProvided = getNodeExecutable.isSuccess
    verboseModeEnabled = true
    assembleScript = "run build"
    nodeInstallDirectory = getNodeExecutable
        .map { file(it).parentFile.parentFile }
        .getOrElse {
            println("Could not find Node.js executable. ${it.message}")
            file(".gradle/nodejs")
        }
}

micronaut {
    runtime("netty")
    testRuntime("kotest5")
    processing {
        incremental(true)
        annotations("$group.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading.set(false)
        convertYamlToJava.set(false)
        precomputeOperations.set(true)
        cacheEnvironment.set(true)
        optimizeClassLoading.set(true)
        deduceEnvironment.set(true)
        optimizeNetty.set(true)
    }
}

jte {
    sourceDirectory.set(file("src/main/jte").toPath())
    targetDirectory.set(layout.buildDirectory.dir("jte-classes").get().asFile.toPath())
    trimControlStructures.set(true)
    packageName.set(group.toString())
    generate()
    jteExtension("gg.jte.models.generator.ModelExtension") {
        property("language", "Kotlin")
    }
}

testlogger {
    theme = ThemeType.MOCHA
    showExceptions = true
    showStackTraces = true
}

testSets {
    create("accessibilityTest")
}

val accessibilityTestImplementation: Configuration = configurations["accessibilityTestImplementation"]
val antJUnit: Configuration by configurations.creating

tasks {
    /* -------------------------------- */
    /* Start: Node/assets configuration */
    /* -------------------------------- */
    named("installFrontend") {
        inputs.files("package.json", "yarn.lock")
        outputs.dir("node_modules")
    }

    val npmAssetsPipeline by named("assembleFrontend") {
        inputs.files(fileTree(layout.projectDirectory.dir("src/main/resources")))
        outputs.files(fileTree(layout.buildDirectory.dir("resources/main/public")))
    }

    val generateMetafile by registering(GenerateAssetsMetadataTask::class) {
        group = "Assets"
        description = "Generate assets metadata file"
        assetsDirectory = layout.buildDirectory.dir("resources/main/public/")
        dependsOn(npmAssetsPipeline)
    }

    val assetsPipeline by registering {
        group = "Assets"
        description = "Executes the complete assets pipeline including manifest generation"

        dependsOn(npmAssetsPipeline, generateMetafile)
    }

    processResources {
        dependsOn(assetsPipeline)
    }
    /* ------------------------------ */
    /* End: Node/assets configuration */
    /* ------------------------------ */

    named("check") {
        dependsOn("diktatCheck")
    }

    named<Test>("test") {
        useJUnitPlatform()
        // See https://kotest.io/docs/extensions/system_extensions.html#system-environment
        jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED")

        // Only generate reports when running on CI. Helps to speed up test execution.
        // https://docs.gradle.org/current/userguide/performance.html#disable_reports
        reports.html.required = runningOnCI
        reports.junitXml.required = runningOnCI
    }

    // Gradle requires that generateJte is run before some tasks
    configureEach {
        if (name == "inspectRuntimeClasspath" || name == "kspKotlin") {
            mustRunAfter("generateJte")
        }
    }

    named<Jar>("jar") {
        dependsOn.add("precompileJte")
        from(fileTree(layout.buildDirectory.file("jte-classes").get().asFile.absolutePath)) {
            include("**/.*.class")
        }
    }

    // Install pre-commit git hooks to run ktlint and detekt
    // https://docs.gradle.org/current/userguide/working_with_files.html#sec:copying_single_file_example
    register<Copy>("configureGitHooksPath") {
        group = "setup"
        description = "Configure git hooks directory"
        shellRun("git", listOf("config", "core.hooksPath", "hooks"))
    }

    register("mergeJUnitReports") {
        val resultsDir = project.layout.buildDirectory.file("test-results/test").get().asFile
        val accessibilityResultsDir = project.layout.buildDirectory.file("test-results/accessibilityTest").get().asFile
        val aggregateFile = "build/test-results/junit.xml"

        doLast {
            ant.withGroovyBuilder {
                "taskdef"(
                    "name" to "junitreport",
                    "classname" to "org.apache.tools.ant.taskdefs.optional.junit.XMLResultAggregator",
                    "classpath" to antJUnit.asPath,
                )

                // generates an XML report
                "junitreport"("tofile" to aggregateFile) {
                    "fileset"(
                        "dir" to resultsDir,
                        "includes" to "TEST-*.xml",
                    )
                    "fileset"(
                        "dir" to accessibilityResultsDir,
                        "includes" to "TEST-*.xml",
                    )
                }
            }
        }
    }
}

dependencies {
    ksp(mn.micronaut.http.validation)
    ksp(mn.micronaut.serde.processor)
    implementation(mn.micronaut.aop)
    implementation(mn.micronaut.kotlin.runtime)
    implementation(mn.micronaut.kotlin.extension.functions)
    implementation(mn.micronaut.serde.jackson)
    implementation(mn.micronaut.views.jte)
    implementation(mn.micronaut.views.htmx)
    implementation(mn.micronaut.management)
    compileOnly(mn.micronaut.http.client)
    testImplementation(mn.micronaut.http.client)

    // To test health indicators
    testImplementation(mn.reactor.test)

    // Creates a dependency provider for graal (org.graalvm.nativeimage:svm)
    compileOnly(mn.graal.asProvider())
    runtimeOnly(mn.logback.classic)
    runtimeOnly(mn.jackson.module.kotlin)

    implementation(kotlin("reflect", kotlinVersion))
    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // jte dependencies
    jteGenerate("gg.jte:jte-models:$jteVersion")
    jteGenerate("gg.jte:jte-native-resources:$jteVersion")
    implementation("gg.jte:jte:$jteVersion")
    implementation("gg.jte:jte-kotlin:$jteVersion")

    // Lucene
    implementation("org.apache.lucene:lucene-core:$luceneVersion")
    implementation("org.apache.lucene:lucene-analysis-common:$luceneVersion")
    implementation("org.apache.lucene:lucene-queryparser:$luceneVersion")
    implementation("org.apache.lucene:lucene-highlighter:$luceneVersion")

    // Markdown
    implementation("com.vladsch.flexmark:flexmark-ext-footnotes:$flexmarkVersion")

    // Content sanitizer
    implementation("org.owasp.antisamy:antisamy:1.7.7")
    implementation("org.owasp.encoder:encoder:1.3.1")

    // Accessibility Tests
    // Manually adding commons-compress due to https://devhub.checkmarx.com/cve-details/CVE-2024-26308/
    accessibilityTestImplementation("org.apache.commons:commons-compress:1.27.1")
    accessibilityTestImplementation("org.seleniumhq.selenium:selenium-java:4.27.0")
    accessibilityTestImplementation("com.deque.html.axe-core:selenium:4.10.1")

    // Apache Ant: to generate a single JUnit report
    antJUnit("org.apache.ant", "ant-junit", "1.10.15")
}
