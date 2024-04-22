package me.okonecny.gradlekeyring

import com.github.javakeyring.BackendNotSupportedException
import com.github.javakeyring.Keyring
import com.github.javakeyring.PasswordAccessException
import org.gradle.api.Project

internal class SecretAccess(
    private val project: Project,
    createKeyring: () -> Keyring
) {
    private var keyringCreated: Boolean = false
    private val keyring: Keyring by lazy {
        keyringCreated = true
        createKeyring()
        // TODO: close keyring access on finish https://discuss.gradle.org/t/clean-way-to-start-up-shut-down-some-resource-around-test-tasks/43932/3
    }

    fun readSecretValue(config: KeyringSecretConfig): String {
        val prop = project.findProperty(config.projectProperty)
        if (prop != null) {
            if (prop is String) {
                return prop
            } else {
                throw IllegalStateException(
                    """
                    Project properties that contain secrets must be of type String.
                    Property %s is of type %s.
                    """.trimIndent().format(config.projectProperty, prop::class.simpleName)
                )
            }
        }

        val env = System.getenv(config.environmentVariable)
        if (env != null) {
            return env
        }

        try {
            return keyring.getPassword(project.keyringServiceName, config.name)
        } catch (e: BackendNotSupportedException) {
            throw SecretAccessException(config, e)
        } catch (e: PasswordAccessException) {
            throw SecretAccessException(config, e)
        }
    }

    fun writeSecretValue(config: KeyringSecretConfig, value: String) {
        try {
            keyring.setPassword(project.keyringServiceName, config.name, value)
        } catch (e: BackendNotSupportedException) {
            throw SecretAccessException(config, e)
        } catch (e: PasswordAccessException) {
            throw SecretAccessException(config, e)
        }
    }

    fun removeSecretFromKeyring(config: KeyringSecretConfig) {
        try {
            keyring.deletePassword(project.keyringServiceName, config.name)
        } catch (e: BackendNotSupportedException) {
            throw SecretAccessException(config, e)
        } catch (e: PasswordAccessException) {
            throw SecretAccessException(config, e)
        }
    }
}

internal val Project.keyringServiceName: String get() = "Gradle Project " + rootProject.name
