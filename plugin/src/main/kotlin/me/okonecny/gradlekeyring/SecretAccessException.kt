package me.okonecny.gradlekeyring

class SecretAccessException(
    config: KeyringSecretConfig,
    cause: Throwable
) : RuntimeException(
    "Error accessing secret $config: " + cause.message,
    cause
)