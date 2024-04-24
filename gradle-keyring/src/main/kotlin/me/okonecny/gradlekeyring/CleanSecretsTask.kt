package me.okonecny.gradlekeyring

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

internal abstract class CleanSecretsTask @Inject constructor(
    private val secretConfigs: Map<String, KeyringSecretConfig>,
    private val secretAccessProvider: Provider<SecretAccess>
) : DefaultTask() {
    init {
        group = "keyring"
        description = "Removes ALL the secrets defined in the project from the keyring."
    }

    @TaskAction
    fun removeSecrets() {
        if (secretConfigs.isEmpty()) return
        val secretAccess = secretAccessProvider.get()
        secretConfigs.values.forEach(secretAccess::removeSecretFromKeyring)
    }
}