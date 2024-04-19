package me.okonecny.gradlekeyring

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * A simple functional test for the 'org.example.greeting' plugin.
 */
class GradleKeyringPluginFunctionalTest {

    @field:TempDir
    lateinit var projectDir: File

    @Test
    fun `can run task in Groovy DSL`() {
        val buildFile = projectDir.resolve("build.gradle")
        val settingsFile = projectDir.resolve("settings.gradle")

        // Set up the test build
        settingsFile.writeText("")
        buildFile.writeText("""
            plugins {
                id('me.okonecny.gradle-keyring')
            }
            keyring {
                secret "default_secret"            
                secret "explicit_secret", "e_s", "EX_SE"            
            }
        """.trimIndent())

        // Run the build
        val result = runGradleTask("listSecretConfigs")

        // Verify the result
        assertTrue(result.output.contains("Secrets defined in this project:"))
    }

    @Test
    fun `can run task in Kotlin DSL`() {
        val buildFile = projectDir.resolve("build.gradle.kts")
        val settingsFile = projectDir.resolve("settings.gradle.kts")
        // Set up the test build
        settingsFile.writeText("")
        buildFile.writeText("""
            plugins {
                id("me.okonecny.gradle-keyring")
            }
            keyring {
                secret("default_secret")            
                secret("explicit_secret").projectProperty("e_s").environmentVariable("EX_SE")            
            }
        """.trimIndent())

        // Run the build
        val result = runGradleTask("listSecretConfigs")

        // Verify the result
        assertTrue(result.output.contains("Secrets defined in this project:"))
    }

    @Test
    fun `can list secret values`() {
        val buildFile = projectDir.resolve("build.gradle.kts")
        val settingsFile = projectDir.resolve("settings.gradle.kts")
        // Set up the test build
        settingsFile.writeText("")
        buildFile.writeText("""
            plugins {
                id("me.okonecny.gradle-keyring")
            }
            keyring {
                secret("default_secret")            
                secret("explicit_secret").projectProperty("e_s").environmentVariable("EX_SE")            
            }
        """.trimIndent())

        // Run the build
        val result = runGradleTask("listSecretValues", "-Pdefault_secret=foo", "-Pe_s=bar")

        // Verify the result
        assertTrue(result.output.contains("Secret values in this project:"))
    }

    private fun runGradleTask(taskName: String, vararg params: String): BuildResult {
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        val arguments = mutableListOf(taskName)
        arguments.addAll(params)
        runner.withArguments(arguments)
        runner.withProjectDir(projectDir)
        val result = runner.build()
        return result
    }
}