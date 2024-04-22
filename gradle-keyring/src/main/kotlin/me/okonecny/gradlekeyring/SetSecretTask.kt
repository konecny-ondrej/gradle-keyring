package me.okonecny.gradlekeyring

import me.okonecny.gradlekeyring.KeyringSecretConfig.Companion.requireValidSecretName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import javax.inject.Inject

internal open class SetSecretTask @Inject constructor(
    private val secretConfigs: Map<String, KeyringSecretConfig>,
    private val secretAccess: SecretAccess
) : DefaultTask() {
    init {
        group = "keyring"
        description = "Sets the values of the secrets defined in the project. Secrets are read from standard input, " +
                "or from environment. This is a security measure so your secrets don't end up in plaintext in your shell history."
    }

    @Option(
        option = "name",
        description = "Name of the secret to set. See the output of listSecretConfigs task."
    )
    @Input
    lateinit var secretName: String

    @TaskAction
    fun setSecret() {
        requireValidSecretName(secretName)
        val config = secretConfigs[secretName] ?: throw NoSuchElementException(
            "There is no configured secret named '%s'.".format(secretName)
        )

        val secretValue = if (System.getenv().containsKey(config.environmentVariable)) {
            System.getenv(config.environmentVariable)!!
        } else {
            System.console()?.readPassword("Enter value of %s", secretName)?.concatToString() ?: readln()
        }

        secretAccess.writeSecretValue(config, secretValue)
    }
}