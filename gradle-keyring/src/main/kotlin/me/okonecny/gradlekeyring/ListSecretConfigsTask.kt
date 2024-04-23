package me.okonecny.gradlekeyring

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

internal open class ListSecretConfigsTask @Inject constructor(
    private val secretConfigs: Map<String, KeyringSecretConfig>
): DefaultTask() {
    init {
        group = "keyring"
        description = "Lists names and configuration (no values) of all secrets defined in the project."
    }

    @TaskAction
    fun listConfiguredSecrets() {
        if (secretConfigs.isEmpty()) {
            println("No secrets were configured in this project.")
            return
        }

        println("Secrets defined in this project:")
        secretConfigs.forEach { (name, config) ->
            println("Keyring name: $name")
            println("\tProject property: ${config.projectProperty}")
            println("\tEnvironment variable: ${config.environmentVariable}")
        }
    }
}