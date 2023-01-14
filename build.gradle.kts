import org.jetbrains.dokka.DokkaDefaults.moduleName

plugins {
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management") apply false
    id("de.fntsoftware.gradle.markdown-to-pdf") apply false
    id("com.github.node-gradle.node") apply false

    kotlin("android") apply false
    kotlin("jvm") apply false
    kotlin("plugin.spring") apply false
    kotlin("plugin.jpa") apply false
    kotlin("multiplatform") apply false
    kotlin("plugin.serialization") apply false
    id("com.android.application") apply false
    id("org.jetbrains.intellij") apply false

    id("org.jetbrains.dokka")

    id("de.menkalian.vela.keygen") apply false
    id("com.gradle.plugin-publish") apply false
    id("com.google.devtools.ksp") apply false
}

tasks.dokkaHtmlMultiModule.configure {
    moduleName.set("Aquila Source Documentation")
}

allprojects {
    group = "de.menkalian.aquila"
    version = "0.0.1"

    pluginManager.withPlugin("java") {
        extensions.getByType(JavaPluginExtension::class)
            .targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    // Configure multiplatform projects
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        extensions.getByType(org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension::class).apply {
            jvm {
                withJava()
            }

            js {
                binaries.library()
                useCommonJs()
                browser()
                nodejs()
            }

            val hostOs = System.getProperty("os.name")
            logger.info("Configuring for build on $hostOs")

            val isMingwX64 = hostOs.startsWith("Windows")
            val nativeTarget = when {
                hostOs == "Mac OS X" -> macosX64("native")
                hostOs == "Linux"    -> linuxX64("native")
                isMingwX64           -> mingwX64("native")
                else                 -> null
            }

            if (nativeTarget == null) {
                logger.error("Native compilation on your OS is not supported.")
            }

            // Configure targets
            nativeTarget?.apply {
                binaries {
                    sharedLib()
                }
            }

            sourceSets {
                getByName("commonMain") {
                    dependencies {
                        implementation(kotlin("stdlib-common"))
                        implementation(kotlin("reflect"))

                        // Include coroutines
                        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

                        // Include serialization if the plugin is there
                        pluginManager.withPlugin("org.jetbrains.kotlin.plugin.serialization") {
                            implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.2")
                            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
                        }
                    }
                }

                getByName("commonTest") {
                    dependencies {
                        implementation(kotlin("test"))
                        implementation(kotlin("test-common"))
                        implementation(kotlin("test-annotations-common"))
                    }
                }
            }
        }
    }

    pluginManager.withPlugin("org.jetbrains.dokka") {
        dependencies.add("dokkaHtmlPlugin", "org.jetbrains.dokka:kotlin-as-java-plugin:1.4.32")
    }

    pluginManager.withPlugin("jacoco") {
        tasks.withType(JacocoReport::class.java) {
            dependsOn(tasks.getByName("test"))

            reports {
                xml.required.set(true)
                csv.required.set(true)
            }
        }

        tasks.getByName("check") {
            dependsOn(tasks.withType(JacocoReport::class.java))
        }
    }

    pluginManager.withPlugin("maven-publish") {
        extensions.getByType(PublishingExtension::class.java).apply {
            repositories {
                maven {
                    url = uri("https://artifactory.menkalian.de/artifactory/draco")
                    name = "artifactory-menkalian"
                    credentials {
                        username = System.getenv("MAVEN_REPO_USER")
                        password = System.getenv("MAVEN_REPO_PASS")
                    }
                }
            }
        }
    }

    pluginManager.withPlugin("org.jetbrains.dokka") {
        val kotlinVersion = "1.7.21"
        dependencies.add("dokkaHtmlPlugin", "org.jetbrains.dokka:kotlin-as-java-plugin:$kotlinVersion")
        tasks.withType(org.jetbrains.dokka.gradle.DokkaTask::class.java).configureEach {
            try {
                dokkaSourceSets.named("main") {
                    sourceLink {
                        localDirectory.set(project.file("src/main/kotlin"))
                        remoteUrl.set(uri("https://github.com/menkalian/draco/blob/main/${projectDir.relativeTo(rootProject.projectDir)}/src/main/kotlin").toURL())
                        remoteLineSuffix.set("#L")
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    tasks.withType(AbstractCopyTask::class) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

}
