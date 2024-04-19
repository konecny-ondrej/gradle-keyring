package me.okonecny.gradlekeyring

data class KeyringSecretConfig(
    val name: String,
    val projectProperty: String,
    val environmentVariable: String
)
