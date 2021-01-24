plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.21"
}

group = "de.menkalian.aquila"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
    maven {
        url = uri("https://kotlin.bintray.com/kotlinx")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
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
