plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.21"
    id("de.menkalian.vela.keygen") version "1.2.0"
}

group = "de.menkalian.aquila"
version = "1.0.2"

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

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.0.1")

    compileOnly("io.ktor:ktor-http-cio-jvm:1.5.0")
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
