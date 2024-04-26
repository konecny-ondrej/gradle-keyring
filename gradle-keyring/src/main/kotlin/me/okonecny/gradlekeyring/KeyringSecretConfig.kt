package me.okonecny.gradlekeyring

data class KeyringSecretConfig(
    val name: String,
    val projectProperty: String,
    val environmentVariable: String
) {
    companion object {
        private val NAME_ALLOWED_CHARS = Regex("^[a-zA-Z0-9_.-]+$")
        private val PROJECT_PROPERTY_ALLOWED_CHARS = Regex("^[a-zA-Z0-9_.-]+$")
        private val ENVIRONMENT_VARIABLE_ALLOWED_CHARS = Regex("^[a-zA-Z0-9_]+$")

        fun requireValidSecretName(name: String) = require(name.matches(NAME_ALLOWED_CHARS)) {
            "Secret name must match ${NAME_ALLOWED_CHARS.pattern}."
        }

        fun requireValidProjectProperty(name: String) = require(name.matches(PROJECT_PROPERTY_ALLOWED_CHARS)) {
            "Project property name must match ${PROJECT_PROPERTY_ALLOWED_CHARS.pattern}"
        }

        fun requireValidEnvironmentVariable(name: String) = require(name.matches(ENVIRONMENT_VARIABLE_ALLOWED_CHARS)) {
            "Environment variable name must match ${ENVIRONMENT_VARIABLE_ALLOWED_CHARS.pattern}"
        }
    }

    init {
        requireValidSecretName(name)
        requireValidProjectProperty(projectProperty)
        requireValidEnvironmentVariable(environmentVariable)
    }
}
