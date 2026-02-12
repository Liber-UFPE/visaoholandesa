plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version embeddedKotlinVersion
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val exposedVersion: String = "1.0.0"

dependencies {
    // Assets pipeline
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    implementation("commons-codec:commons-codec:1.21.0")
    implementation("org.apache.tika:tika-core:3.2.3")
    implementation("org.apache.tika:tika-parsers-standard-package:3.2.3")
    implementation("org.apache.logging.log4j:log4j-core:2.25.3")
    implementation("com.lordcodes.turtle:turtle:0.10.0")

    // Manually adding commons-compress due to https://devhub.checkmarx.com/cve-details/CVE-2024-26308/
    implementation("org.apache.commons:commons-compress:1.28.0")

    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    runtimeOnly("com.mysql:mysql-connector-j:9.6.0")

    // Add the Ant JUnit dependency. This was removed in Gradle 9.0.0 and
    // now needs to be included manually.
    implementation("org.apache.ant:ant-junit:1.10.15")
}
