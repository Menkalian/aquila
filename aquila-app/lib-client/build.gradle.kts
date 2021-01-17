plugins {
    id("com.android.library")
    kotlin("android")
    `maven-publish`
    id("de.menkalian.auriga")
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

auriga {
    loggingConfig {
        mode = "DEFAULT_ON"
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
}