package me.okonecny.gradlekeyring

import org.apache.tools.ant.filters.StringInputStream
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.InputStream
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * A simple functional test for the 'org.example.greeting' plugin.
 */
class GradleKeyringPluginFunctionalTest {

    @field:TempDir
    lateinit var projectDir: File

    private lateinit var originalStdin: InputStream

    @BeforeTest
    fun setup() {
        originalStdin = System.`in`
    }

    @AfterTest
    fun cleanup() {
        System.setIn(originalStdin)
    }

    @Test
    fun `can run task in Groovy DSL`() {
        val buildFile = projectDir.resolve("build.gradle")
        val settingsFile = projectDir.resolve("settings.gradle")

        // Set up the test build
        settingsFile.writeText("")
        buildFile.writeText(
            """
            plugins {
                id('me.okonecny.gradle-keyring')
            }
            keyring {
                secret "default_secret"            
                secret "explicit_secret", "e_s", "EX_SE"            
            }
        """.trimIndent()
        )

        // Run the build
        val result = runGradleTask("listSecretConfigs")

        // Verify the result
        assertTrue(result.output.contains("Secrets defined in this project:"))
    }

    @Test
    fun `can run task in Kotlin DSL`() {
        setUpTestProject()

        // Run the build
        val result = runGradleTask("listSecretConfigs")

        // Verify the result
        assertTrue(result.output.contains("Secrets defined in this project:"))
    }

    @Test
    fun `can list secret values`() {
        setUpTestProject()

        // Run the build
        val result = runGradleTask("listSecretValues", listOf("-Pdefault_secret=foo", "-Pe_s=bar"))

        // Verify the result
        assertTrue(result.output.contains("Secret values in this project:"))
    }

    @Test
    fun `can set secret values`() {
        System.setIn(StringInputStream("baz" + System.lineSeparator()))

        setUpTestProject()

        try {
            // Run the build
            runGradleTask(
                "setSecret",
                listOf("--name=explicit_secret", "--stacktrace"),
                mapOf(
                    "EX_SE" to "baz"
                )
            )
            val result = runGradleTask("listSecretValues")
            // Verify the result
            assertTrue(result.output.contains("baz"))
        } finally {
            runGradleTask("cleanSecrets")
        }

    }

    private fun setUpTestProject() {
        val buildFile = projectDir.resolve("build.gradle.kts")
        val settingsFile = projectDir.resolve("settings.gradle.kts")
        // Set up the test build
        settingsFile.writeText("")
        buildFile.writeText(
            """
                plugins {
                    id("me.okonecny.gradle-keyring")
                }
                keyring {
                    secret("default_secret")            
                    secret("explicit_secret").projectProperty("e_s").environmentVariable("EX_SE")            
                }
            """.trimIndent()
        )
    }

    private fun runGradleTask(
        taskName: String,
        params: List<String> = emptyList(),
        env: Map<String, String> = emptyMap()
    ): BuildResult {
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withEnvironment(env)
        val arguments = mutableListOf(taskName)
        arguments.addAll(params)
        runner.withArguments(arguments)
        runner.withProjectDir(projectDir)
        val result = runner.build()
        return result
    }
}