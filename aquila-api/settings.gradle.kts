rootProject.name = "aquila-api"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url = uri("http://server.menkalian.de:8081/artifactory/menkalian")
            name = "artifactory-menkalian"
        }
    }
}

