package me.okonecny.gradlekeyring

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

internal open class CleanSecretsTask @Inject constructor(
    private val secretConfigs: Map<String, KeyringSecretConfig>,
    private val secretAccess: SecretAccess
) : DefaultTask() {
    init {
        group = "keyring"
        description = "Removes ALL the secrets defined in the project from the keyring."
    }

    @TaskAction
    fun removeSecrets() {
        secretConfigs.values.forEach(secretAccess::removeSecretFromKeyring)
    }
}