package me.okonecny.gradlekeyring

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

internal open class ListSecretValuesTask @Inject constructor(
    private val secretConfigs: Map<String, KeyringSecretConfig>,
    private val secretAccess: SecretAccess
) : DefaultTask() {
    init {
        group = "keyring"
        description = "Lists the values of all secrets defined in the project."
    }

    @TaskAction
    fun listSecretValues() {
        println("Secret values in this project:")
        secretConfigs.forEach {(name, config) ->
            println("\t" + name + ": " + secretAccess.readSecretValue(config))
        }
    }
}