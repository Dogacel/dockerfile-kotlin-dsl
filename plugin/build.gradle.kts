plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    `java-gradle-plugin`

    // Apply the Kotlin JVM plugin to add support for Kotlin.
    kotlin("jvm") version "1.9.23"
    id("com.gradle.plugin-publish") version "1.2.1"
}

group = "io.github.dogacel"
version = "0.0.1"

gradlePlugin {
    website = "https://github.com/Dogacel/dockerfile-kotlin-dsl"
    vcsUrl = "https://github.com/Dogacel/dockerfile-kotlin-dsl.git"
    plugins {
        create("dockerfileKotlinDslPlugin") {
            id = "io.github.dogacel.dsl.dockerfile"
            displayName = "Dockerfile Kotlin DSL"
            description = "Streamlined Dockerfile generation using Kotlin DSL inside Gradle build scripts."
            tags = listOf("docker", "dockerfile", "kotlin", "dsl", "build", "config")
            implementationClass = "io.github.dogacel.dsl.dockerfile.DockerfileDslPlugin"
        }
    }
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Add a source set for the functional test suite
val functionalTestSourceSet =
    sourceSets.create("functionalTest") {
    }

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["functionalTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

gradlePlugin.testSourceSets.add(functionalTestSourceSet)

tasks.named<Task>("check") {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}

tasks.named<Test>("test") {
    // Use JUnit Jupiter for unit tests.
    useJUnitPlatform()
}
