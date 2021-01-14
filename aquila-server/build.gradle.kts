plugins {
    java
    kotlin("jvm") version "1.4.21"
}

group = "de.menkalian.aquila"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(ktor("server-core"))
}

fun ktor(module: String, version: String = "1.5.0"): String = "io.ktor:ktor-$module:$version"
