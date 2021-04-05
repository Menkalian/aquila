rootProject.name = "aquila"

include("client")
include("server")

//include("client:app-mobile")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        jcenter()
        maven {
            name = "artifactory-menkalian"
            url = uri("http://server.menkalian.de:8081/artifactory/menkalian")
        }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.android")) {
                useModule("com.android.tools.build:gradle:4.1.3")
            }
        }
    }
}
