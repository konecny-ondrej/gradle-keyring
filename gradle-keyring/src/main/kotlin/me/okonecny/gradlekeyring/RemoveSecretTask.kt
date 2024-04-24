package me.okonecny.gradlekeyring

import me.okonecny.gradlekeyring.KeyringSecretConfig.Companion.requireValidSecretName
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import javax.inject.Inject

internal abstract class RemoveSecretTask @Inject constructor(
    private val secretConfigs: Map<String, KeyringSecretConfig>,
    private val secretAccessProvider: Provider<SecretAccess>
) : DefaultTask() {
    init {
        group = "keyring"
        description = "Removes the secrets defined in the project from the keyring."
    }

    @Option(
        option = "name",
        description = "Name of the secret to remove. See the output of listSecretConfigs task."
    )
    @Input
    lateinit var secretName: String

    @TaskAction
    fun removeSecret() {
        requireValidSecretName(secretName)
        val config = secretConfigs[secretName] ?: throw NoSuchElementException(
            "There is no configured secret named '%s'.".format(secretName)
        )

        secretAccessProvider.get().removeSecretFromKeyring(config)
    }
}