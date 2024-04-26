plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    `java-gradle-plugin`
    `maven-publish`

    // Apply the Kotlin JVM plugin to add support for Kotlin.
    alias(libs.plugins.jvm)

    if (System.getProperty("useKeyring") != null) {
        alias(libs.plugins.keyring)
    } else {
        println("Using dummy keyring implementation.")
        println("First run the publishAllPublicationsToProjectLocalRepository to bootstrap the project, then")
        println("add -Dorg.gradle.jvmargs=\"-DuseKeyring\" to the subsequent gradle commands.")
        id("DummyKeyring")
    }
}

group = "me.okonecny"
version = libs.plugins.keyring.get().version
description = "Store/load your secrets to/from your system's keyring."

keyring {
    secret("gradle.publish.key")
    secret("gradle.publish.secret")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

publishing {
    repositories {
        maven {
            name = "ProjectLocal"
            url = uri(layout.buildDirectory.asFile.get().resolve("repository"))
        }
    }
}

dependencies {
    implementation("com.github.javakeyring:java-keyring:1.0.3")

    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

gradlePlugin {
    // Define the plugin
    val greeting by plugins.creating {
        id = libs.plugins.keyring.get().pluginId
        implementationClass = "me.okonecny.gradlekeyring.GradleKeyringPlugin"
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
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
