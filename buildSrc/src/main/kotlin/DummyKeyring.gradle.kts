data class DummyConfig(
    val foo: String = "bar"
)

open class KeyringExtension {
    val secrets: Map<String, String> = mapOf()
    val configs: Map<String, DummyConfig> = mapOf()

    fun secret(name: String) = secret(name, name, name.uppercase())

    fun secret(
        name: String,
        projectProperty: String = "",
        environmentVariable: String = ""
    ): DummyConfig = DummyConfig()

    fun DummyConfig.projectProperty(propertyName: String): DummyConfig = DummyConfig()
    fun DummyConfig.environmentVariable(envVarName: String): DummyConfig = DummyConfig()
}

val keyring = extensions.create<KeyringExtension>("keyring")
