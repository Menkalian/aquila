plugins {
    id("com.android.application")
    kotlin("android")
    `maven-publish`
}

apply(from = rootProject.file("versioning.gradle.kts"))

android {
    compileSdkVersion(30)
    buildToolsVersion("30.0.3")

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("SIGNING_KEYSTORE_LOCATION") ?: "keystore.jks")
            storePassword = System.getenv("SIGNING_KEYSTORE_PASS")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASS")
        }
    }

    defaultConfig {
        applicationId = "de.menkalian.aquila.wear"
        minSdkVersion(26)
        targetSdkVersion(30)
        versionCode(extra["buildNumber"].toString().toInt())
        versionName("1.0.0_${extra["buildNumber"]}")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix(".debug")
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")!!
        }
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
                from(components["release_apk"])

                groupId = "de.menkalian.aquila"
                artifactId = "app-android"
                version = "1.0.0_${extra["buildNumber"]}"
            }
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib"))
    implementation("com.google.android.support:wearable:2.8.1")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.wear:wear:1.1.0")
    compileOnly("com.google.android.wearable:wearable:2.8.1")

    implementation(project(":lib-client"))
}
