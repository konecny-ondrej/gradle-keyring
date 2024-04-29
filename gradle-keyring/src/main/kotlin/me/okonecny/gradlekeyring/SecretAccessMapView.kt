package me.okonecny.gradlekeyring

import me.okonecny.gradlekeyring.KeyringSecretConfig.Companion.requireValidSecretName
import org.gradle.api.Project
import org.gradle.api.provider.Provider

internal class SecretAccessMapView(
    secretConfigsInput: Map<String, KeyringSecretConfig>,
    private val secretAccess: Provider<SecretAccess>,
    private val project: Project,
) : Map<String, String> {
    private val secretConfigs = secretConfigsInput
    override val keys: Set<String> by secretConfigs::keys
    override val size: Int by secretConfigs::size
    override val entries: Set<Map.Entry<String, String>>
        get() = secretConfigs.entries.map { entry ->
            object : Map.Entry<String, String> {
                override val key: String = entry.key
                override val value: String = secretAccess.get().readSecretValue(project, entry.value)
            }
        }.toSet()
    override val values: Collection<String>
        get() = entries.map(Map.Entry<String, String>::value)

    override fun containsKey(key: String): Boolean = secretConfigs.containsKey(key)

    override fun isEmpty(): Boolean = secretConfigs.isEmpty()

    override fun containsValue(value: String): Boolean = values.contains(value)

    override fun get(key: String): String {
        requireValidSecretName(key)
        return try {
            secretAccess.get().readSecretValue(
                project,
                secretConfigs[key] ?: throw NoSuchElementException(
                    "There is no configured secret named '%s'. Use the keyring {} block to define it.".format(
                        key
                    )
                )
            )
        } catch (e: SecretAccessException) {
            project.logger.error(e.localizedMessage, e)
            // This is for situations where the credentials are not yet set, but are evaluated eagerly in the build script.
            // If we didn't return something, you wouldn't be able to even run the setSecretValue task.
            "<not set>"
        }
    }
}