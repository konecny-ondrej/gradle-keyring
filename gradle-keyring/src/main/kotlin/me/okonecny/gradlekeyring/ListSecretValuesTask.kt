package me.okonecny.gradlekeyring

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

internal abstract class ListSecretValuesTask @Inject constructor(
    private val secretConfigs: Map<String, KeyringSecretConfig>,
    private val secretAccessProvider: Provider<SecretAccess>
) : DefaultTask() {
    init {
        group = "keyring"
        description = "Lists the values of all secrets defined in the project."
    }

    @TaskAction
    fun listSecretValues() {
        if (secretConfigs.isEmpty()) {
            println("No secrets were configured in this project.")
            return
        }
        println("Secret values in this project:")
        val secretAccess = secretAccessProvider.get()
        val exceptions = mutableListOf<SecretAccessException>()
        secretConfigs.forEach {(name, config) ->
            print("\t$name: ")
            try {
                println(secretAccess.readSecretValue(project, config))
            } catch (e: SecretAccessException) {
                println("<not set, use './gradlew setSecretValue --name=$name' to set it>")
                exceptions.add(e)
            }
        }
        if (exceptions.isNotEmpty()) {
            val summaryException = SecretAccessException(
                "Could not read secrets:\n\t" + exceptions.map(Exception::message).joinToString("\n\t")
            )
            exceptions.forEach(summaryException::addSuppressed)
            throw summaryException
        }
    }
}