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
        val exceptions = mutableListOf<SecretAccessException>()
        secretConfigs.values.forEach { secretConfig ->
            try {
                secretAccess.removeSecretFromKeyring(secretConfig)
            } catch (e: SecretAccessException) {
                exceptions.add(e)
            }
        }
        if (exceptions.isNotEmpty()) {
            val summaryException = SecretAccessException(
                "Could not clean secrets:\n\t" + exceptions.map(Exception::message).joinToString("\n\t")
            )
            exceptions.forEach(summaryException::addSuppressed)
            throw summaryException
        }
    }
}