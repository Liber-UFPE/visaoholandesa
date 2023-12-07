import com.adarshr.gradle.testlogger.theme.ThemeType
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.github.vacxe.buildtimetracker.reporters.markdown.MarkdownConfiguration
import io.micronaut.gradle.docker.MicronautDockerfile
import io.micronaut.gradle.docker.NativeImageDockerfile
import java.lang.System.getenv
import java.time.Duration
import java.util.Optional
import kotlin.jvm.optionals.getOrElse

plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.allopen") version "1.9.21"
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.2.0"
    id("gg.jte.gradle") version "3.1.5"
    id("io.micronaut.aot") version "4.2.0"
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
}

val runningOnCI: Boolean = getenv().getOrDefault("CI", "false").toBoolean()

val javaVersion: Int = 21

val kotlinVersion: String = properties["kotlinVersion"] as String
val micronautVersion: String = properties["micronautVersion"] as String
val jteVersion: String = properties["jteVersion"] as String

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
}
tasks.withType<NativeImageDockerfile> {
    // Oracle's images provide access to G1 GC and other features.
    graalImage.set("container-registry.oracle.com/graalvm/native-image:$javaVersion")
    // Using a distroless image generates a "mostly-static" image:
    // https://micronaut-projects.github.io/micronaut-gradle-plugin/latest/#_build_mostly_static_native_executables
    // https://www.graalvm.org/latest/reference-manual/native-image/guides/build-static-executables/
    baseImage("gcr.io/distroless/cc-debian12")
    environmentVariable("MICRONAUT_ENVIRONMENTS", "docker")
}
tasks.register("dockerImageNameNative") {
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
tasks.register("dockerImageName") { dependsOn("dockerImageNameNative") } // This is how Gradle add aliases.

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

// Install pre-commit git hooks to run ktlint and detekt
// https://docs.gradle.org/current/userguide/working_with_files.html#sec:copying_single_file_example
tasks.register<Copy>("installGitHooks") {
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
    compileOnly(mn.micronaut.http.client)
    testImplementation(mn.micronaut.http.client)

    // Creates a dependency provider for graal (org.graalvm.nativeimage:svm)
    compileOnly(mn.graal.asProvider())
    runtimeOnly(mn.logback.classic)
    runtimeOnly(mn.jackson.module.kotlin)

    implementation(kotlin("reflect", kotlinVersion))
    implementation(kotlin("stdlib-jdk8", kotlinVersion))

    // jte dependencies
    jteGenerate("gg.jte:jte-models:$jteVersion")
    jteGenerate("gg.jte:jte-native-resources:$jteVersion")
    implementation("gg.jte:jte:$jteVersion")
    implementation("gg.jte:jte-kotlin:$jteVersion")

    // Accessibility Tests
    accessibilityTestImplementation("org.seleniumhq.selenium:selenium-java:4.15.0")
    accessibilityTestImplementation("com.deque.html.axe-core:selenium:4.8.0")
}
