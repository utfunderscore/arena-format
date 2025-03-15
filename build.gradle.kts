plugins {
    kotlin("jvm") version "2.1.0"
    `java-library`
}

group = "org.readutf.buildstore"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.slf4j:slf4j-api:2.0.17")
    api("com.michael-bull.kotlin-result:kotlin-result:2.0.1")
    api("org.jetbrains.kotlin:kotlin-reflect:2.1.10")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}
