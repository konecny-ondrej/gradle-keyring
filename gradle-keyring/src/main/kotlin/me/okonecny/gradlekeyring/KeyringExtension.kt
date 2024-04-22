package me.okonecny.gradlekeyring

import org.gradle.api.reflect.HasPublicType
import org.gradle.api.reflect.TypeOf
import javax.inject.Inject

interface PublicKeyringExtension {
    val secrets: Map<String, String>
    val configs: Map<String, KeyringSecretConfig>

    fun secret(name: String) = secret(name, name, name.uppercase())

    fun secret(
        name: String,
        projectProperty: String = name,
        environmentVariable: String = name.uppercase()
    ): KeyringSecretConfig

    fun KeyringSecretConfig.projectProperty(propertyName: String): KeyringSecretConfig
    fun KeyringSecretConfig.environmentVariable(envVarName: String): KeyringSecretConfig
}

internal open class KeyringExtension @Inject internal constructor(
    secretAccess: SecretAccess
) : PublicKeyringExtension, HasPublicType {
    private val privateConfigs = mutableMapOf<String, KeyringSecretConfig>()
    override val configs: Map<String, KeyringSecretConfig> by ::privateConfigs
    override val secrets: Map<String, String> = SecretAccessMapView(privateConfigs, secretAccess)

    override fun secret(
        name: String,
        projectProperty: String,
        environmentVariable: String
    ): KeyringSecretConfig {
        val config = KeyringSecretConfig(name, projectProperty, environmentVariable)
        privateConfigs[name] = config
        return config
    }

    override infix fun KeyringSecretConfig.projectProperty(propertyName: String): KeyringSecretConfig {
        val config = copy(projectProperty = propertyName)
        privateConfigs[name] = config
        return config
    }

    override infix fun KeyringSecretConfig.environmentVariable(envVarName: String): KeyringSecretConfig {
        val config = copy(environmentVariable = envVarName)
        privateConfigs[name] = config
        return config
    }

    override fun getPublicType(): TypeOf<*> = TypeOf.typeOf(PublicKeyringExtension::class.java)
}