plugins {
    val kotlinVersion = "1.5.0-M2"

    // Kotlin Plugins
    kotlin("jvm") version kotlinVersion apply false
    kotlin("android") version kotlinVersion apply false
    kotlin("plugin.spring") version kotlinVersion apply false

    // Menkalian Plugins
}

allprojects {
    group = "de.menkalian.aquila"

    repositories {
        mavenCentral()
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.plugin.spring") {
        configurations {
            getByName("compileOnly") {
                extendsFrom(configurations.getByName("annotationProcessor"))
            }
        }

        tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "11"
            }
        }
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        dependencies {
            add("implementation", kotlin("reflect"))
            add("implementation", kotlin("stdlib"))
            add("implementation", kotlin("stdlib-jdk7"))
            add("implementation", kotlin("stdlib-jdk8"))
        }
    }

    pluginManager.withPlugin("maven-publish") {
        extensions.getByType(PublishingExtension::class.java).apply {
            repositories {
                maven {
                    url = uri("http://server.menkalian.de:8081/artifactory/aquila")
                    name = "artifactory-menkalian"
                    credentials {
                        username = System.getenv("MAVEN_REPO_USER")
                        password = System.getenv("MAVEN_REPO_PASS")
                    }
                }
            }
        }
    }

    pluginManager.withPlugin("java") {
        extensions.getByType(JavaPluginExtension::class).apply {
            withJavadocJar()
            withSourcesJar()
        }

        dependencies {
            add("testImplementation", "org.junit.jupiter:junit-jupiter-api:5.7.0")
            add("testImplementation", "org.junit.jupiter:junit-jupiter-params:5.7.0")
            add("testRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine:5.7.0")
        }

        tasks {
            withType(Test::class.java) {
                useJUnitPlatform()
            }
        }
    }

    pluginManager.withPlugin("jacoco") {
        tasks.withType(JacocoReport::class.java) {
            dependsOn(tasks.getByName("test"))

            reports {
                xml.isEnabled = true
                csv.isEnabled = true
            }
        }

        tasks.getByName("check") {
            dependsOn(tasks.withType(JacocoReport::class.java))
        }
    }
}
