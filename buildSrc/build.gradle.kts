plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version embeddedKotlinVersion
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Assets pipeline
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("commons-codec:commons-codec:1.16.0")
    implementation("org.apache.tika:tika-core:2.9.1")
    implementation("org.apache.tika:tika-parsers-standard-package:2.9.1")
    implementation("org.apache.logging.log4j:log4j-core:2.22.1")

    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:0.44.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.44.1")
    runtimeOnly("com.mysql:mysql-connector-j:8.2.0")
}