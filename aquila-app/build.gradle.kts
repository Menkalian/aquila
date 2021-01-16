// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        jcenter()
        maven {
            url = uri("http://server.menkalian.de:8081/artifactory/menkalian")
            name = "artifactory-menkalian"
        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.1")
        classpath(kotlin("gradle-plugin", version = "1.4.21"))
//        classpath "de.menkalian.auriga:auriga-gradle-plugin:1.0.1"
    }
}

allprojects {
//    apply plugin: "de.menkalian.auriga"

    repositories {
        google()
        jcenter()
    }

//    auriga {
//        loggingConfig {
//            mode = "DEFAULT_ON"
//        }
//    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
