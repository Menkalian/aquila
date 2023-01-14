rootProject.name = "aquila"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            name = "Menkalian"
            url = uri("https://artifactory.menkalian.de/artifactory/menkalian")
        }
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.51.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "Menkalian"
            url = uri("https://artifactory.menkalian.de/artifactory/menkalian")
        }
    }
}

// client implementations
include(
//    ":client-android",
    ":client-web",
)

// documentation
include(
    ":doc-clientdev",
    ":doc-plugindev",
    ":doc-project",
    ":doc-specs",
)

// general libraries
include(
    ":lib-client",
    ":lib-plugin",
    ":lib-shared",
)

// plugins
include(
    ":plugin-gradle",
//    ":plugin-idea",
)

// server components
include(
    ":server-game",
    ":server-plugins",
)
