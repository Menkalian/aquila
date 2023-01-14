plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")

    id("org.jetbrains.dokka")

    `maven-publish`
}

kotlin {
    fun ktor(module: String) = "io.ktor:ktor-$module:_"

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(ktor("client-core"))
                implementation(ktor("client-serialization"))
                implementation(ktor("client-logging"))
                implementation(ktor("client-websockets"))

                //implementation(project(":lib-shared"))
            }
        }

//        val jsMain by getting {
//            dependencies {
//                implementation(ktor("client-js"))
//            }
//        }

//        val jvmMain by getting {
//            dependencies {
//                implementation(ktor("client-cio"))
//                implementation("org.slf4j:slf4j-api:_")
//
//                api(project(":lib-shared"))
//            }
//        }
//
//        val nativeMain by getting {
//            dependencies {
//                implementation(ktor("client-curl"))
//            }
//        }
    }
}