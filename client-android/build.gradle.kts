import java.util.Properties

plugins {
    kotlin("android")
    id("com.android.application")
    id("com.google.devtools.ksp")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "de.menkalian.aquila.android"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true

            val props = Properties()
            val propFile = rootProject.file("keystore.local.properties")
            if (propFile.exists()) {
                props.load(propFile.inputStream())
            }

            storeFile = file(props["path"] ?: "")
            storePassword = props["password"]?.toString() ?: ""
            keyAlias = props["key"]?.toString() ?: ""
            keyPassword = props["keypass"]?.toString() ?: ""
        }
    }

    buildTypes {
        named("debug") {
            applicationIdSuffix = ".dbg"
        }
        named("release") {
            signingConfig = signingConfigs.getByName("release")

            isMinifyEnabled = true
            isDebuggable = false
            isShrinkResources = true

            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(AndroidX.core.ktx)

    implementation("de.menkalian.sagitta:loglib:_")
}

ksp {
}