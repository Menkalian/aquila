import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    application
    `maven-publish`
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("de.menkalian.auriga") version "1.0.2"
}

group = "de.menkalian.aquila"
version = "1.0.0"
setProperty("mainClassName", "de.menkalian.aquila.server.ApplicationKt")

auriga {
    loggingConfig {
        mode = "DEFAULT_OFF"
    }
}

repositories {
    jcenter()
    mavenCentral()
    maven {
        url = uri("https://kotlin.bintray.com/kotlinx")
    }
    mavenLocal()
    maven {
        url = uri("http://server.menkalian.de:8081/artifactory/aquila")
        name = "artifactory-menkalian"
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")

    implementation(ktor("server-core"))
    implementation(ktor("server-netty"))
    implementation(ktor("serialization"))
    implementation(ktor("websockets"))
    implementation(ktor("client-cio"))
    implementation(ktor("client-serialization"))

    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("de.menkalian.aquila:aquila-api:1.0.2")
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

tasks.getByName<ShadowJar>("shadowJar") {
    archiveBaseName.set("aquila-server")
    archiveClassifier.set("")
    archiveVersion.set("")
}

tasks.withType(KotlinCompile::class.java).configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}

fun ktor(module: String, version: String = "1.5.0"): String = "io.ktor:ktor-$module:$version"
