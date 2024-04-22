package me.okonecny.gradlekeyring

data class KeyringSecretConfig(
    val name: String,
    val projectProperty: String,
    val environmentVariable: String
) {
    companion object {
        private val NAME_ALLOWED_CHARS = Regex("^[a-zA-Z0-9_]+$")

        fun requireValidSecretName(name: String) = require(name.matches(NAME_ALLOWED_CHARS)) {
            "Secret name must match ${NAME_ALLOWED_CHARS.pattern}."
        }
    }

    init {
        requireValidSecretName(name)
    }
}
