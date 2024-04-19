package me.okonecny.gradlekeyring

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

internal open class SetSecretTask @Inject constructor(
    private val secretConfigs: Map<String, KeyringSecretConfig>,
    private val secretAccess: SecretAccess
): DefaultTask() {
    init {
        group = "keyring"
        description = "Sets the values of the secrets defined in the project."
    }

    @TaskAction
    fun setSecret() {
        TODO()
    }
}