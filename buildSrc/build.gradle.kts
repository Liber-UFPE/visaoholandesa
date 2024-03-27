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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("commons-codec:commons-codec:1.16.1")
    implementation("org.apache.tika:tika-core:2.9.1")
    implementation("org.apache.tika:tika-parsers-standard-package:2.9.1")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("com.lordcodes.turtle:turtle:0.9.0")

    // Manually adding commons-compress due to https://devhub.checkmarx.com/cve-details/CVE-2024-26308/
    implementation("org.apache.commons:commons-compress:1.26.1")

    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:0.48.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.49.0")
    runtimeOnly("com.mysql:mysql-connector-j:8.3.0")
}
