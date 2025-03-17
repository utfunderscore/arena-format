plugins {
    kotlin("jvm") version "2.1.0"
    `java-library`
    `maven-publish`
    signing
}

group = "org.readutf.arenaformat"
version = "1.0.4"

repositories {
    mavenCentral()
    maven { url = uri("https://mvn.utf.lol/releases") }
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.slf4j:slf4j-api:2.0.17")
    api("com.michael-bull.kotlin-result:kotlin-result:2.0.1")
    api("org.jetbrains.kotlin:kotlin-reflect:2.1.10")

    api("dev.hollowcube:polar:1.13.0")
    api("net.minestom:minestom-snapshots:39d445482f")
    api("dev.hollowcube:schem:2.0.0")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "org.readutf.arenaformat"
            artifactId = "arenaformat"
            version = this.version
        }
    }

    repositories {
        maven {
            name = "utfRepoReleases"
            url = uri("https://mvn.utf.lol/releases")
            credentials(PasswordCredentials::class)
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}
