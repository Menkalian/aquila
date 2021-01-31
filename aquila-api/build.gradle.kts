plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.21"
    id("de.menkalian.vela.keygen") version "1.2.0"
}

group = "de.menkalian.aquila"
version = "1.0.1"

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
    maven {
        url = uri("https://kotlin.bintray.com/kotlinx")
    }
}

keygen {
    targetDir = File(buildDir, "generated/vela/keyobject/values/java").toURI()

    furtherConfigs {
        create("topics") {
            sourceDir = File(projectDir, "src/main/topicKeys").toURI()
            targetDir = File(buildDir, "generated/vela/keyobject/topics/java").toURI()

            separator = "/"
            targetPackage = "de.menkalian.aquila.api.websocket.topics"
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.0.1")
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
