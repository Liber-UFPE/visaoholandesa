import br.ufpe.liber.tasks.GenerateAssetsMetadataTask
import br.ufpe.liber.tasks.GenerateBooksJsonTask
import com.adarshr.gradle.testlogger.theme.ThemeType
import com.bmuschko.gradle.vagrant.tasks.VagrantUp
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.gradle.node.npm.task.NpmTask
import io.github.vacxe.buildtimetracker.reporters.markdown.MarkdownConfiguration
import java.lang.System.getenv
import java.nio.file.Files
import java.time.Duration

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.allopen") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.3.2"
    id("gg.jte.gradle") version "3.1.9"
    id("io.micronaut.aot") version "4.3.2"
    // Apply GraalVM Native Image plugin. Micronaut already adds it, but
    // adding it explicitly allows to control which version is used.
    id("org.graalvm.buildtools.native") version "0.10.1"
    // Provides better test output
    id("com.adarshr.test-logger") version "4.0.0"
    // Code Coverage:
    // https://github.com/Kotlin/kotlinx-kover
    id("org.jetbrains.kotlinx.kover") version "0.7.6"
    // Code Inspections
    // https://detekt.dev/
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
    // Task graph utility
    // https://github.com/dorongold/gradle-task-tree
    id("com.dorongold.task-tree") version "2.1.1"
    // Manages Vagrant boxes:
    // https://plugins.gradle.org/plugin/com.bmuschko.vagrant
    id("com.bmuschko.vagrant") version "3.0.0"
    // Easily add new test sets
    // https://github.com/unbroken-dome/gradle-testsets-plugin
    id("org.unbroken-dome.test-sets") version "4.1.0"
    // Report task timings
    // https://github.com/vacxe/build-time-tracker
    id("io.github.vacxe.build-time-tracker") version "0.0.4"
    // To check dependency updates
    // https://github.com/ben-manes/gradle-versions-plugin
    id("com.github.ben-manes.versions") version "0.51.0"
    // To manage docker images
    // https://github.com/bmuschko/gradle-docker-plugin
    id("com.bmuschko.docker-remote-api") version "9.4.0"
    // SonarQube/SonarCloud plugin
    // https://github.com/SonarSource/sonar-scanner-gradle
    id("org.sonarqube") version "4.4.1.3373"
    // Add diktat
    // https://github.com/saveourtool/diktat
    id("com.saveourtool.diktat") version "2.0.0"
    // To run npm/node/js tasks
    // https://github.com/node-gradle/gradle-node-plugin
    id("com.github.node-gradle.node") version "7.0.2"
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

        property(
            "sonar.sources",
            sourceSets
                .main
                .get()
                .allSource
                .srcDirs
                .filterNot {
                    // Exclude generated files
                    it.absolutePath.startsWith(layout.buildDirectory.asFile.get().absolutePath)
                }
                .filter(File::exists),
        )

        property(
            "sonar.tests",
            sourceSets
                // Include any source set that contains test in its name.
                // For example, "test", "integrationTest", etc.
                .filter { sourceSet -> sourceSet.name.contains("test", ignoreCase = true) }
                .flatMap { sourceSet ->
                    sourceSet
                        .allSource
                        .srcDirs
                        .filterNot { dir ->
                            // Exclude generated files
                            dir.absolutePath.startsWith(layout.buildDirectory.asFile.get().absolutePath)
                        }
                        .filter(File::exists)
                },
        )

        // See docs here:
        // https://docs.sonarsource.com/sonarqube/latest/project-administration/analysis-scope/#code-coverage-exclusion
        property(
            "sonar.coverage.exclusions",
            listOf("**/*Generated*", "**/tasks/*.*"),
        )

        property(
            "sonar.exclusions",
            listOf("src/**/*.sql", "src/main/**/sitemap.xml", "src/main/**/tasks/*.*"),
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
tasks.named("check") {
    dependsOn("diktatCheck")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    // See https://kotest.io/docs/extensions/system_extensions.html#system-environment
    jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED")

    // Only generate reports when running on CI. Helps to speed up test execution.
    // https://docs.gradle.org/current/userguide/performance.html#disable_reports
    reports.html.required = runningOnCI
    reports.junitXml.required = runningOnCI
}

/* -------------------------------- */
/* Start: Node/assets configuration */
/* -------------------------------- */
node {
    version = "18.19.0"
    download = false
}

tasks {
    val npmAssetsPipeline by registering(NpmTask::class) {
        group = "Assets"
        description = "Executes assets pipeline using npm"

        inputs.files(fileTree(layout.projectDirectory.dir("src/main/resources")))
        args = listOf("run", "assetsPipeline")
        outputs.files(fileTree(layout.buildDirectory.dir("resources/main/public")))

        dependsOn("npmInstall")
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
}
/* ------------------------------ */
/* End: Node/assets configuration */
/* ------------------------------ */

testSets {
    create("accessibilityTest")
}
val accessibilityTestImplementation: Configuration = configurations["accessibilityTestImplementation"]

// See https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html
graalvmNative {
    toolchainDetection.set(false)
    binaries {
        named("main") {
            fallback.set(false)
            richOutput.set(true)
            buildArgs.addAll("--verbose", "-march=native", "--gc=G1")
            jvmArgs.add("-XX:MaxRAMPercentage=100")
            if (runningOnCI) {
                // A little extra verbose on CI to prevent jobs being killed
                // due to the lack of output (since native-image creation can
                // take a long time to complete).
                jvmArgs.add("-Xlog:gc*")
                // 7GB is what is available when using Github-hosted runners:
                // https://docs.github.com/en/actions/using-github-hosted-runners/about-github-hosted-runners#supported-runners-and-hardware-resources
                jvmArgs.addAll("-Xms7G", "-Xmx7G")
            } else {
                // `gc` is less verbose than `gc*`, and good enough for local builds.
                jvmArgs.add("-Xlog:gc")
                // 16G is a good chunk of memory, but reducing GC speeds up
                // the native image generation.
                jvmArgs.addAll("-Xms16G", "-Xmx16G")
            }
        }
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
    jteExtension("gg.jte.nativeimage.NativeResourcesExtension")
    jteExtension("gg.jte.models.generator.ModelExtension") {
        property("language", "Kotlin")
    }
}
// Gradle requires that generateJte is run before some tasks
tasks.configureEach {
    if (name == "inspectRuntimeClasspath" || name == "kspKotlin") {
        mustRunAfter("generateJte")
    }
}
tasks.named<Jar>("jar") {
    dependsOn.add("precompileJte")
    from(fileTree(layout.buildDirectory.file("jte-classes").get().asFile.absolutePath)) {
        include("**/.*.class")
    }
}

testlogger {
    theme = ThemeType.MOCHA
    showExceptions = true
    showStackTraces = true
}

buildTimeTracker {
    markdownConfiguration.set(
        MarkdownConfiguration(
            reportFile = reporting.file("build-times.md").absolutePath,
            minDuration = Duration.ofMillis(0),
            withTableLabels = true,
            sorted = true,
            take = Int.MAX_VALUE,
        ),
    )
}

/* ---------------------------- */
/* Start: Vagrant configuration */
/* ---------------------------- */
val vagrantBoxDir: File = layout.buildDirectory.dir("vagrant").get().asFile
val jarsDir: File = File(vagrantBoxDir, "build/libs")
val scriptsDir: File = File(vagrantBoxDir, "scripts")

tasks.register("createVagrantDirs") {
    group = "Vagrant"
    description = "Create directories required by vagrantUp task"

    doLast {
        Files.createDirectories(jarsDir.toPath())
        Files.createDirectories(scriptsDir.toPath())
    }
}

tasks.register<Copy>("copyAppJar") {
    group = "Vagrant"
    description = "Copy application jar to Vagrant directories"

    from(layout.buildDirectory.dir("libs"))
    include("*.jar")
    into(jarsDir)
    dependsOn(tasks.named("createVagrantDirs"))
    dependsOn(tasks.named("runnerJar"))
}

tasks.register<Copy>("copyVagrantScripts") {
    group = "Vagrant"
    description = "Copy application configurations to Vagrant directories"

    from(layout.projectDirectory.dir("scripts"))
    into(scriptsDir)
}

tasks.named<VagrantUp>("vagrantUp") {
    boxDir = vagrantBoxDir
    dependsOn(tasks.named("copyAppJar"), tasks.named("copyVagrantScripts"))
}
/* -------------------------- */
/* End: Vagrant configuration */
/* -------------------------- */

tasks.register<JavaExec>("generateSitemaps") {
    group = "Application"
    description = "Generate Sitemap.xml"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "br.ufpe.liber.tasks.Sitemaps"
}

// Install pre-commit git hooks to run ktlint and detekt
// https://docs.gradle.org/current/userguide/working_with_files.html#sec:copying_single_file_example
tasks.register<Copy>("installGitHooks") {
    group = "setup"
    description = "Install pre-commit git hooks"
    from(layout.projectDirectory.file("scripts/pre-commit"))
    into(layout.projectDirectory.dir(".git/hooks/"))
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    rejectVersionIf {
        val version = this.currentVersion
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?\$".toRegex()
        val possiblySnapshot = "\\d{8}".toRegex()
        !stableKeyword && !version.matches(regex) || version.matches(possiblySnapshot)
    }
}

val generateBooksJsonTask by tasks.registering(GenerateBooksJsonTask::class)

dependencies {
    // TEMP: Brings logback 1.4.14. Remove when micronaut-core updates.
    implementation(platform("io.micronaut.logging:micronaut-logging-bom:1.2.3"))

    ksp(mn.micronaut.http.validation)
    ksp(mn.micronaut.serde.processor)
    implementation(mn.micronaut.aop)
    implementation(mn.micronaut.kotlin.runtime)
    implementation(mn.micronaut.kotlin.extension.functions)
    implementation(mn.micronaut.serde.jackson)
    implementation(mn.micronaut.views.jte)
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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

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
    implementation("org.owasp.antisamy:antisamy:1.7.5")
    implementation("org.owasp.encoder:encoder:1.2.3")

    // Accessibility Tests
    accessibilityTestImplementation("org.seleniumhq.selenium:selenium-java:4.18.0")
    accessibilityTestImplementation("com.deque.html.axe-core:selenium:4.8.2")
}
