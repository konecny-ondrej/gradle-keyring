package me.okonecny.gradlekeyring

import com.github.javakeyring.BackendNotSupportedException
import com.github.javakeyring.Keyring
import com.github.javakeyring.PasswordAccessException
import org.gradle.api.Project
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import java.io.Serializable

internal abstract class SecretAccess : BuildService<SecretAccess.Parameters>, AutoCloseable {
    open class Parameters : BuildServiceParameters, Serializable {
        lateinit var projectKeyringServiceName: String
    }

    private val keyringLazy = lazy(Keyring::create)
    private val keyring: Keyring by keyringLazy

    override fun close() {
        if (keyringLazy.isInitialized()) keyring.close()
    }

    fun readSecretValue(project: Project, config: KeyringSecretConfig): String {
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

        return readSecretValueFromKeyring(config)
    }

    private fun readSecretValueFromKeyring(config: KeyringSecretConfig): String {
        try {
            synchronized(this) {
                return keyring.getPassword(parameters.projectKeyringServiceName, config.name)
            }
        } catch (e: BackendNotSupportedException) {
            throw SecretAccessException(config, e)
        } catch (e: PasswordAccessException) {
            throw SecretAccessException(config, e)
        }
    }

    fun writeSecretValue(config: KeyringSecretConfig, value: String) {
        try {
            synchronized(this) {
                keyring.setPassword(parameters.projectKeyringServiceName, config.name, value)
            }
        } catch (e: BackendNotSupportedException) {
            throw SecretAccessException(config, e)
        } catch (e: PasswordAccessException) {
            throw SecretAccessException(config, e)
        }
    }

    fun removeSecretFromKeyring(config: KeyringSecretConfig) {
        try {
            synchronized(this) {
                keyring.deletePassword(parameters.projectKeyringServiceName, config.name)
            }
        } catch (e: BackendNotSupportedException) {
            throw SecretAccessException(config, e)
        } catch (e: PasswordAccessException) {
            throw SecretAccessException(config, e)
        }
    }
}
