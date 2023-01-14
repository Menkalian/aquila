plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")

    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")

    id("org.jetbrains.dokka")

    id("de.menkalian.vela.keygen")

    `maven-publish`
}

springBoot {
    buildInfo()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

//    implementation(project(":lib-shared"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.jar)
            artifact(tasks.kotlinSourcesJar)
            artifact(tasks.bootJar)
        }
    }
}

tasks.bootJar.configure {
    archiveClassifier.set("boot")
}