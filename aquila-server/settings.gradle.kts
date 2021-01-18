rootProject.name = "aquila-server"
pluginManagement{
    repositories {
        gradlePluginPortal()
        maven {
            name="artifactory-menkalian"
            url = uri("http://server.menkalian.de:8081/artifactory/menkalian")
        }
    }
}

