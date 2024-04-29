# Gradle Keyring plugin

This plugin deals with 2 issues:
1. It saves the secrets used in your project into your system's keyring,
so you don't have to have them stored in plaintext on your disk. All thanks to the [java-keyring](https://github.com/javakeyring/java-keyring) library.

2. Also, it allows you to have your secrets saved specifically for each project,
instead of hijacking the whole user profile for one project by putting
the secrets to `~/.gradle/gradle.properties`.

> [!WARNING]
> Your secrets will only be encrypted _at rest_.
> 
> Depending on your platform any, Java program (Mac), or any program with
> access to the keyring (Linux), can read your secrets.

## Usage

In your build script you can use the plugin by making your buildscript look like this:

```kotlin
plugins {
    id("me.okonecny.gradle-keyring") version "0.1"
}
```

Then later in your buildscript you define the secrets that you want to use:

```kotlin
keyring {
    // You can specify everything explicitly.
    secret("gradle.publish.key").projectProperty("gradle.key").environmentVariable("GRADLE_KEY")
    secret("gradle.publish.secret").environmentVariable("GRADLE_SECRET")
    // Or let the plugin generate the project property and environment variable for you.
    secret("my_repo_user")
    secret("my_repo_password")
}
```

And finally you can use your secrets in various places that require credentials, e.g. when publishing:
```kotlin
publishing {
    repositories {
        maven {
            // All the usual stuff... and then:
            credentials {
                uasername = keying.secrets["my_repo_user"]
                password = keying.secrets["my_repo_password"]
            }
        }
    }
}

tasks.named("publishPlugins").configure {
    doFirst {
        project.ext["gradle.publish.key"] = keyring.secrets["gradle.publish.key"]
        project.ext["gradle.publish.secret"] = keyring.secrets["gradle.publish.secret"]
    }
}
```

Each time you ask the plugin for a secret, it will look in these places, in this order:
1. Project properties
2. Environment variables
3. Keyring

Each `secret` you define will have a default project property name and environment variable name
based on the secret name. You can also specify them explicitly by calling the appropriate methods (see above).

Because the plugin first looks into project properties and environment variables, it is safe to use
the plugin in CI pipelines where keyring is usually unavailable. This makes it possible to pass the secrets
through commandline or environment.

## Tasks

The plugin defines a few tasks that make the secret management easier.

- `listSecretConfigs` - See what secrets you have defined in your project and their corresponding project properties
and environment variable names. No secrets are exposed.
- `listSecretValues` - See the secret values. :warning: Use with care, this does expose the secret.
- `setSecretValue --name=secret_name` - Set the value of the secret in your keyring. Reads the secret from standard input,
so the secrets don't end up in your command history in plaintext.
- `removeSecret --name=secret_name` - Removes the secret from your keyring.
- `cleanSecrets` - Remove all secrets defined in the project from your keyring. Useful if you want to quit a project.

## Bootstrapping this project

Because this project itself uses the plugin it implements, we run into a chicken-and-egg problem.
Fortunately the secrets are not actually needed for building. So you can build the project first,
then you can use the freshly-built plugin to load the secrets:

1. Boostrap the plugin: `./gradlew publishAllPublicationsToProjectLocalRepository` .
2. Run a task that requires secrets: `./gradlew listSecretValues -Dorg.gradle.jvmargs="-DuseKeyring"`.

> [!NOTE]
> This bootstrap process is needed in this project only, you don't need it in other projects using the plugin.