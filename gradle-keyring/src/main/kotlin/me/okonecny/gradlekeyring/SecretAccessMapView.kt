package me.okonecny.gradlekeyring

internal class SecretAccessMapView(
    secretConfigsInput: Map<String, KeyringSecretConfig>,
    private val secretAccess: SecretAccess
) : Map<String, String> {
    private val secretConfigs = secretConfigsInput
    override val keys: Set<String> by secretConfigs::keys
    override val size: Int by secretConfigs::size
    override val entries: Set<Map.Entry<String, String>>
        get() = secretConfigs.entries.map { entry ->
            object : Map.Entry<String, String> {
                override val key: String = entry.key
                override val value: String = secretAccess.readSecretValue(entry.value)
            }
        }.toSet()
    override val values: Collection<String>
        get() = entries.map(Map.Entry<String, String>::value)

    override fun containsKey(key: String): Boolean = secretConfigs.containsKey(key)

    override fun isEmpty(): Boolean = secretConfigs.isEmpty()

    override fun containsValue(value: String): Boolean = values.contains(value)

    override fun get(key: String): String? {
        return secretAccess.readSecretValue(
            secretConfigs[key] ?: return null
        )
    }
}