import com.adarshr.gradle.testlogger.theme.ThemeType
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.bmuschko.gradle.vagrant.tasks.VagrantUp
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.gradle.node.npm.task.NpmTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.github.vacxe.buildtimetracker.reporters.markdown.MarkdownConfiguration
import io.micronaut.gradle.docker.MicronautDockerfile
import io.micronaut.gradle.docker.NativeImageDockerfile
import java.lang.System.getenv
import java.nio.file.Files
import java.time.Duration
import java.util.Optional
import kotlin.jvm.optionals.getOrElse

plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.allopen") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("com.google.devtools.ksp") version "1.9.21-1.0.16"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.2.1"
    id("io.micronaut.aot") version "4.2.1"
    id("gg.jte.gradle") version "3.1.6"
    // Apply GraalVM Native Image plugin. Micronaut already adds it, but
    // adding it explicitly allows to control which version is used.
    id("org.graalvm.buildtools.native") version "0.9.28"
    // Provides better test output
    id("com.adarshr.test-logger") version "4.0.0"
    // Code Coverage:
    // https://github.com/Kotlin/kotlinx-kover
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
    // Code Inspections
    // https://detekt.dev/
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
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
    id("com.github.ben-manes.versions") version "0.50.0"
    // To manage docker images
    // https://github.com/bmuschko/gradle-docker-plugin
    id("com.bmuschko.docker-remote-api") version "9.4.0"
    // To run npm/node/js tasks
    // https://github.com/node-gradle/gradle-node-plugin
    id("com.github.node-gradle.node") version "7.0.1"
    // SonarQube/SonarCloud plugin
    // https://github.com/SonarSource/sonar-scanner-gradle
    id("org.sonarqube") version "4.4.1.3373"
}

val runningOnCI: Boolean = getenv().getOrDefault("CI", "false").toBoolean()
val loglevel: String = properties.getOrDefault("org.gradle.logging.level", "LIFECYCLE") as String

val javaVersion: Int = 17

val kotlinVersion: String = properties["kotlinVersion"] as String
val jteVersion: String = properties["jteVersion"] as String
val exposedVersion: String = properties["exposedVersion"] as String
val luceneVersion: String = properties["luceneVersion"] as String
val flexmarkVersion: String = properties["flexmarkVersion"] as String
val kotestVersion: String = properties["kotestVersion"] as String

version = "0.1"
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
        property("sonar.projectKey", "Liber-UFPE_visaoholandesa")
        property("sonar.organization", "liber-ufpe")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.junit.reportPaths", "build/test-results/test/*.xml")

        // https://docs.sonarsource.com/sonarcloud/enriching/test-coverage/test-coverage-parameters/#javakotlinscalajvm
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/kover/report.xml")

        // https://docs.sonarsource.com/sonarcloud/enriching/external-analyzer-reports/#kotlin
        property("sonar.kotlin.detekt.reportPaths", "build/reports/detekt/detekt.xml")

        // https://docs.sonarsource.com/sonarcloud/advanced-setup/languages/kotlin/#specifying-the-kotlin-source-code-version
        property("sonar.kotlin.source.version", "1.9")

        property(
            "sonar.sources",
            sourceSets.main.get().allSource.srcDirs.filterNot {
                // Exclude generated files
                it.absolutePath.startsWith(layout.buildDirectory.asFile.get().absolutePath)
            },
        )

        property(
            "sonar.tests",
            sourceSets.test.get().allSource.srcDirs.filterNot {
                // Exclude generated files
                it.absolutePath.startsWith(layout.buildDirectory.asFile.get().absolutePath)
            },
        )
    }
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

        dependsOn("npmInstall")
        inputs.files(fileTree(layout.projectDirectory.dir("src/main/resources")))
        args = listOf("run", "assetsPipeline")
        outputs.files(fileTree(layout.buildDirectory.dir("resources/main/public")))
    }

    val generateMetafile by registering(JavaExec::class) {
        group = "Assets"
        description = "Generate assets metadata file"

        classpath = sourceSets.main.get().runtimeClasspath
        mainClass = "br.ufpe.liber.tasks.GenerateAssetsMetadata"
        args(layout.buildDirectory.file("resources/main/public/").get().asFile.absolutePath)
    }

    val assetsPipeline by registering {
        group = "Assets"
        description = "Executes the complete assets pipeline including manifest generation"

        dependsOn(npmAssetsPipeline)
        finalizedBy(generateMetafile)
    }

    processResources {
        dependsOn(assetsPipeline)
    }
}

/* ------------------------------ */
/* End: Node/assets configuration */
/* ------------------------------ */

/* ------------------------- */
/* Start: Test configuration */
/* ------------------------- */
tasks.named<Test>("test") {
    useJUnitPlatform()
    // See https://kotest.io/docs/extensions/system_extensions.html#system-environment
    jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED")

    // Only generate reports when running on CI. Helps to speed up test execution.
    // https://docs.gradle.org/current/userguide/performance.html#disable_reports
    reports.html.required = runningOnCI
    reports.junitXml.required = runningOnCI
}

testSets {
    create("accessibilityTest")
}
val accessibilityTestImplementation: Configuration = configurations["accessibilityTestImplementation"]

koverReport {
    filters {
        excludes {
            classes("br.ufpe.liber.model.Database*", "br.ufpe.liber.Sitemaps")
        }
    }
}
/* ----------------------- */
/* End: Test configuration */
/* ----------------------- */

/* --------------------------- */
/* Start: Docker configuration */
/* --------------------------- */
fun registry(): Optional<String> = Optional.ofNullable(getenv("REGISTRY")).map { it.lowercase() }
fun imageName(): Optional<String> = Optional.ofNullable(getenv("IMAGE_NAME")).map { it.lowercase() }
fun imageNames(): List<String> {
    return registry()
        .flatMap { registry -> imageName().map { name -> "$registry/$name".lowercase() } }
        .map { imageTag -> listOf("$imageTag:latest", "$imageTag:$version") }
        .getOrElse { emptyList() }
}

tasks.named<DockerBuildImage>("dockerBuild") { images.addAll(imageNames()) }
tasks.named<DockerBuildImage>("dockerBuildNative") { images.addAll(imageNames()) }
tasks.withType<MicronautDockerfile> {
    // Amazon Correto is slightly larger than `eclipse-temurin` image,
    // but seems better maintained and with no vulnerabilities reported.
    baseImage.set("amazoncorretto:$javaVersion")
    environmentVariable("MICRONAUT_ENVIRONMENTS", "docker")
    healthcheck(Dockerfile.Healthcheck("curl -I -f http://localhost:8080/ || exit 1"))
}
tasks.withType<NativeImageDockerfile> {
    // Oracle's images provide access to G1 GC and other features.
    graalImage.set("container-registry.oracle.com/graalvm/native-image:$javaVersion")
    // Using a distroless image generates a "mostly-static" image:
    // https://micronaut-projects.github.io/micronaut-gradle-plugin/latest/#_build_mostly_static_native_executables
    // https://www.graalvm.org/latest/reference-manual/native-image/guides/build-static-executables/
    baseImage("gcr.io/distroless/cc-debian12")
    environmentVariable("MICRONAUT_ENVIRONMENTS", "docker")
    healthcheck(Dockerfile.Healthcheck("curl -I -f http://localhost:8080/ || exit 1"))
}
tasks.register("dockerImageNameNative") {
    group = "help"
    description = "Shows the name of the Docker image"
    doFirst {
        val images = tasks.named<DockerBuildImage>("dockerBuildNative").get().images.get()
        val maybeRegistry = registry()
        val imageName = if (maybeRegistry.isPresent) {
            images.find { it.contains(maybeRegistry.get()) }
        } else {
            images.first()
        }
        // No need to show `:latest`, so remove if it is present
        println(imageName?.replace(":latest", ""))
    }
}
tasks.register("dockerImageName") { // This is how Gradle add aliases.
    group = "help"
    description = "Shows the name of the Docker image"
    dependsOn("dockerImageNameNative")
}
/* ------------------------- */
/* End: Docker configuration */
/* ------------------------- */

// See https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html
graalvmNative {
    toolchainDetection.set(false)
    binaries {
        named("main") {
            fallback.set(false)
            richOutput.set(true)
            buildArgs.addAll("--verbose", "-march=native")
            jvmArgs.add("-XX:MaxRAMPercentage=100")
            if (javaVersion == 21) {
                buildArgs.addAll("--gc=G1")
            }
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

/* ------------------------ */
/* Start: jte configuration */
/* ------------------------ */
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

tasks.withType<ShadowJar>().configureEach {
    // See Gradle docs:
    // https://docs.gradle.org/8.5/dsl/org.gradle.api.tasks.bundling.Zip.html#org.gradle.api.tasks.bundling.Zip:zip64
    isZip64 = true
}

/* ---------------------- */
/* End: jte configuration */
/* ---------------------- */

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
    mainClass = "br.ufpe.liber.Sitemaps"
}

// Install pre-commit git hooks to run ktlint and detekt
// https://docs.gradle.org/current/userguide/working_with_files.html#sec:copying_single_file_example
tasks.register<Copy>("installGitHooks") {
    group = "Project Setup"
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

dependencies {
    ksp(mn.micronaut.http.validation)
    ksp(mn.micronaut.serde.processor)

    implementation(mn.micronaut.aop)
    implementation(mn.micronaut.kotlin.runtime)
    implementation(mn.micronaut.kotlin.extension.functions)
    implementation(mn.micronaut.serde.jackson)
    implementation(mn.micronaut.views.jte)
    implementation(mn.micronaut.management)

    // Later use the catalog. For now, it will declare the dependency
    // manually to avoid CVE-2023-45960:
    // runtimeOnly(mn.logback.classic)
    implementation("ch.qos.logback:logback-core:1.4.14")
    implementation(kotlin("reflect", kotlinVersion))
    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Exposed
    // https://github.com/JetBrains/Exposed
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")

    // Lucene
    implementation("org.apache.lucene:lucene-core:$luceneVersion")
    implementation("org.apache.lucene:lucene-analysis-common:$luceneVersion")
    implementation("org.apache.lucene:lucene-queryparser:$luceneVersion")
    implementation("org.apache.lucene:lucene-highlighter:$luceneVersion")

    // Markdown
    implementation("com.vladsch.flexmark:flexmark-ext-footnotes:$flexmarkVersion")

    // Content sanitizer
    implementation("org.owasp.antisamy:antisamy:1.7.4")
    implementation("org.owasp.encoder:encoder:1.2.3")

    implementation("commons-codec:commons-codec:1.16.0")
    implementation("org.lz4:lz4-pure-java:1.8.0")
    implementation("org.apache.tika:tika-core:2.9.1")
    implementation("org.apache.tika:tika-parsers-standard-package:2.9.1")

    // jte dependencies
    implementation("gg.jte:jte:$jteVersion")
    implementation("gg.jte:jte-kotlin:$jteVersion")

    jteGenerate("gg.jte:jte-models:$jteVersion")
    jteGenerate("gg.jte:jte-native-resources:$jteVersion")

    // Creates a dependency provider for graal (org.graalvm.nativeimage:svm)
    compileOnly(mn.graal.asProvider())
    compileOnly(mn.micronaut.http.client)

    runtimeOnly("com.mysql:mysql-connector-j:8.2.0")
    runtimeOnly(mn.jackson.module.kotlin)

    // Kotest latest version
    testImplementation(platform("io.kotest:kotest-bom:$kotestVersion"))
    testImplementation(mn.micronaut.http.client)

    // Accessibility Tests
    accessibilityTestImplementation("org.seleniumhq.selenium:selenium-java:4.16.1")
    accessibilityTestImplementation("com.deque.html.axe-core:selenium:4.8.0")
}
