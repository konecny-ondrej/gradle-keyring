package me.okonecny.gradlekeyring

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * A simple unit test for the 'org.example.greeting' plugin.
 */
class GradleKeyringPluginTest {
    @Test
    fun `plugin registers tasks`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("me.okonecny.gradle-keyring")

        // Verify the result
        assertNotNull(project.tasks.findByName("listSecretConfigs"))
        assertNotNull(project.tasks.findByName("listSecretValues"))
        assertNotNull(project.tasks.findByName("setSecretValue"))
    }
}