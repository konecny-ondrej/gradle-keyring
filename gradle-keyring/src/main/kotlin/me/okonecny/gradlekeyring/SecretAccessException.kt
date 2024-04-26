package me.okonecny.gradlekeyring

class SecretAccessException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    constructor(
        config: KeyringSecretConfig,
        cause: Throwable
    ) : this("Error accessing secret $config: " + cause.message, cause)
}