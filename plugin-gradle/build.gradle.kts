plugins {
    `java-gradle-plugin`
    kotlin("jvm")

    id("org.jetbrains.dokka")

    `maven-publish`
    id("com.gradle.plugin-publish")
}

gradlePlugin {
    plugins {
        create("aquila") {
            id = "de.menkalian.aquila.plugin"
            implementationClass = "de.menkalian.aquila.gradle.AquilaGradlePlugin"
            displayName = "Aquila Plugin Development Plugin"
            description = """
                Gradle-Plugin to help with developing plugins for the aquila game framework.
                See https://github.com/menkalian/aquila/ or https://developer.aquila.menkalian.de for more information.
            """.trimIndent()
        }
    }
}
