package me.okonecny.gradlekeyring

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

internal abstract class ListSecretValuesTask @Inject constructor(
    private val secretConfigs: Map<String, KeyringSecretConfig>,
    private val secretAccessProvider: Provider<SecretAccess>
) : DefaultTask() {
    init {
        group = "keyring"
        description = "Lists the values of all secrets defined in the project."
    }

    @TaskAction
    fun listSecretValues() {
        if (secretConfigs.isEmpty()) {
            println("No secrets were configured in this project.")
            return
        }
        println("Secret values in this project:")
        val secretAccess = secretAccessProvider.get()
        secretConfigs.forEach {(name, config) ->
            println("\t" + name + ": " + secretAccess.readSecretValue(project, config))
        }
    }
}