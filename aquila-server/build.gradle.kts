plugins {
    java
    application
    `maven-publish`
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "2.0.1"
    id("de.menkalian.auriga") version "1.0.1"
}

group = "de.menkalian.aquila"
version = "1.0.0"

application.mainClass.set("de.menkalian.aquila.server.ApplicationKt")

repositories {
    jcenter()
    mavenCentral()
    maven {
        url = uri("https://kotlin.bintray.com/kotlinx")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(ktor("server-core"))
    implementation(ktor("server-netty"))
    implementation(ktor("serialization"))
    implementation(ktor("client-cio"))
    implementation(ktor("client-serialization"))
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.1")
}

publishing {
    repositories {
        maven {
            url = uri("http://server.menkalian.de:8081/artifactory/aquila")
            name = "artifactory-menkalian"
            authentication {
                credentials {
                    username = System.getenv("MAVEN_REPO_USER")
                    password = System.getenv("MAVEN_REPO_PASS")
                }
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks.jar {
    archiveFileName.set("server.jar")
}

fun ktor(module: String, version: String = "1.5.0"): String = "io.ktor:ktor-$module:$version"
