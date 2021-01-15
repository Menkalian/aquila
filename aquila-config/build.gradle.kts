plugins {
    kotlin("jvm") version "1.4.21"
    `maven-publish`
}

group = "de.menkalian.aquila"
version = "1.0.0"

repositories {
    mavenCentral()
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

dependencies {
    implementation(kotlin("stdlib"))
}
