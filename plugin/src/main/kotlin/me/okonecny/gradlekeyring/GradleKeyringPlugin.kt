package me.okonecny.gradlekeyring

import com.github.javakeyring.Keyring
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.reflect.TypeOf

/**
 * Plugin to access secrets from project properties, environment variables, or your system's keyring.
 */
class GradleKeyringPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        val secretAccess = SecretAccess(project, Keyring::create)

        val keyringExtension = project.extensions.create(
            TypeOf.typeOf(PublicKeyringExtension::class.java),
            "keyring",
            KeyringExtension::class.java,
            secretAccess
        )

        project.tasks.create(
            "listSecretConfigs",
            ListSecretConfigsTask::class.java,
            keyringExtension.configs
        )

        project.tasks.create(
            "listSecretValues",
            ListSecretValuesTask::class.java,
            keyringExtension.configs,
            secretAccess
        )

        project.tasks.create(
            "setSecretValue",
            SetSecretTask::class.java,
            keyringExtension.configs,
            secretAccess
        )
    }
}