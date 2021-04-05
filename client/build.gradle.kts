plugins {
    id("com.android.application") apply false
    kotlin("android") apply false
}

subprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven {
            url = uri("http://server.menkalian.de:8081/artifactory/menkalian")
            name = "artifactory-menkalian"
        }
    }
}

tasks.register("clean", Delete::class) {
    group = "build"
    delete(rootProject.buildDir)
}
