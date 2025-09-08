plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version embeddedKotlinVersion
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val exposedVersion: String = "0.61.0"

dependencies {
    // Assets pipeline
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("commons-codec:commons-codec:1.19.0")
    implementation("org.apache.tika:tika-core:3.2.2")
    implementation("org.apache.tika:tika-parsers-standard-package:3.2.2")
    implementation("org.apache.logging.log4j:log4j-core:2.25.1")
    implementation("com.lordcodes.turtle:turtle:0.10.0")

    // Manually adding commons-compress due to https://devhub.checkmarx.com/cve-details/CVE-2024-26308/
    implementation("org.apache.commons:commons-compress:1.28.0")

    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    runtimeOnly("com.mysql:mysql-connector-j:9.4.0")
}
