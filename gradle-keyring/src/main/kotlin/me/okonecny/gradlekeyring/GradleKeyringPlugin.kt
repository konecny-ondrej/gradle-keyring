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
        val secretAccess = project.gradle.sharedServices.registerIfAbsent(
            "secretAccess",
            SecretAccess::class.java
        ) { buildServiceSpec ->
            buildServiceSpec.parameters { params ->
                params.projectKeyringServiceName = project.keyringServiceName
            }
        }

        val keyringExtension = project.extensions.create(
            TypeOf.typeOf(PublicKeyringExtension::class.java),
            "keyring",
            KeyringExtension::class.java,
            secretAccess,
            project
        )

        project.tasks.create(
            "listSecretConfigs",
            ListSecretConfigsTask::class.java,
            keyringExtension.configs
        )

        val listSecretValuesTask = project.tasks.create(
            "listSecretValues",
            ListSecretValuesTask::class.java,
            keyringExtension.configs,
            secretAccess
        )
        listSecretValuesTask.usesService(secretAccess)

        val setSecretValueTask = project.tasks.create(
            "setSecretValue",
            SetSecretTask::class.java,
            keyringExtension.configs,
            secretAccess
        )
        setSecretValueTask.usesService(secretAccess)

        val removeSecretTask = project.tasks.create(
            "removeSecret",
            RemoveSecretTask::class.java,
            keyringExtension.configs,
            secretAccess
        )
        removeSecretTask.usesService(secretAccess)

        val cleanSecretsTask = project.tasks.create(
            "cleanSecrets",
            CleanSecretsTask::class.java,
            keyringExtension.configs,
            secretAccess
        )
        cleanSecretsTask.usesService(secretAccess)
    }
}

internal val Project.keyringServiceName: String get() = "Gradle Project " + rootProject.name
