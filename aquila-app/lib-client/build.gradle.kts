plugins {
    id("com.android.library")
    id("de.menkalian.auriga")
    kotlin("android")
    kotlin("plugin.serialization") version "1.4.21"
    `maven-publish`
}

android {
    compileSdkVersion(30)
    buildToolsVersion("30.0.3")

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("SIGNING_KEYSTORE_LOCATION") ?: "keystore.jks")
            storePassword = System.getenv("SIGNING_KEYSTORE_PASS")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASS")

            this.isV1SigningEnabled = true
            this.isV2SigningEnabled = true
        }
    }

    defaultConfig {
        minSdkVersion(26)
        targetSdkVersion(30)
        versionCode(1)
        versionName("1.0.0")

        testInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("debug") {
            resValue("string", "aquila_server_url", "https://10.0.0.2:8083")
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")!!

            resValue("string", "aquila_server_url", "https://server.menkalian.de:8083")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

auriga {
    loggingConfig {
        mode = "DEFAULT_OFF"
    }
}

afterEvaluate {
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
                from(components["release"])

                groupId = "de.menkalian.aquila"
                artifactId = "lib-client"
                version = "1.0.0"
            }
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib"))
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.2")

    implementation("io.ktor:ktor-client-android:1.5.0")
    implementation("io.ktor:ktor-client-serialization:1.5.0")
}