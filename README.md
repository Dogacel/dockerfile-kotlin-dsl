# Dockerfile Kotlin DSL

Streamline docker image building process for _Gradle_ based build systems with extended configuration options with
minimal external scripting.

This gradle plugin allows you to define your Dockerfile using Kotlin DSL inside your gradle configuration

## Usage

```kotlin

plugins {
    id("io.github.dogacel:dockerfile-kotlin-dsl") version "0.0.1"
}

// Define a root dockerfile named `Dockerfile`.
dockerfile {
    from("openjdk:21-jdk-slim")
    workdir("/app")

    +"Copy the JAR file into the Docker Image"
    copy {
        source = "app.jar"
        destination = "/app/app.jar"
    }

    +"Download assets"
    val assetPath = when (System.getenv("MODE")) {
        "local" -> "/first_100_assets.zip"
        "staging" -> "/compressed_assets.zip"
        else -> "/full_assets.zip"
    }

    run("curl", "-o", "assets.zip", "https://example.com/" + assetPath)
    run("unzip", "assets.zip", "-d", "/app/assets", "&&", "rm", "assets.zip")

    when (System.getenv("MODE")) {
        "local" -> expose(80)
        else -> {}
    }

    expose(443)

    cmd("java", "-jar", "app.jar")
}
```

To create a dockerfile, run `./gradlew dockerfileGenerate`. You can pass a `--name=Test.dockerfile` parameter to specify
the file name. The file will be created under your project root.

## Why?

Regular `Dockerfile`s do not allow configuring image build process. Multiple docker images might built to serve
different purposes, such as minimal test environment images, local images, integration test images, performance tweaked
load test images, production ready images, localized images for different audiences etc.

## Example

Let's say we are creating a microservice that serves some static metadata for our _Book Store_ mobile application.
Regularly, a person would create a single Dockerfile for their specific needs or they might create multiple dockerfiles
with a lot of duplication to customize for their specific needs.

```dockerfile
# Use the official OpenJDK 21 image from Docker Hub
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the Docker image
COPY app.jar /app/app.jar

# Download assets
RUN curl -o assets.zip https://example.com/path/to/assets.zip
RUN unzip assets.zip -d /app/assets && rm assets.zip

EXPOSE 443

# Specify the command to run the JAR file
CMD ["java", "-jar", "app.jar"]
```

There are some things to consider:

- Assets might be too big for the testing environment. We can use a smaller subset of assets or compressed assets for
  non-prod environments.
- TLS is hard to configure for local environments.
- Environment variables can be set to change logging behavior in test / prod environments.

It is possible to fix those issues by distributing different Dockerfiles for each environment, however, over time
entropy will take over and those Dockerfiles will start to deviate each other and will cause maintenance overhead due to
high levels of code duplication.

Let's create a _Dockerfile_ definition in our `build.gradle.kts`,

```kotlin
dockerfile {
    from("openjdk:21-jdk-slim")
    workdir("/app")

    +"Copy the JAR file into the Docker Image"
    copy {
        source = "app.jar"
        destination = "/app/app.jar"
    }

    cmd("java", "-jar", "app.jar")
}
```

Let's download the right assets for our environment.

```kotlin
dockerfile {
    ...
    +"Download assets"
    val assetPath =
        when (System.getenv("MODE")) {
            "local" -> "/first_100_assets.zip"
            "staging" -> "/compressed_assets.zip"
            else -> "/full_assets.zip"
        }

    run("curl", "-o", "assets.zip", "https://example.com/" + assetPath)
    run("unzip", "assets.zip", "-d", "/app/assets", "&&", "rm", "assets.zip")
}
```

Now, let's enable HTTP endpoint for our local environment so we don't need to setup TLS.

```kotlin
dockerfile {
    ...
    when (System.getenv("MODE")) {
        "local" -> expose(80)
        else -> {}
    }

    expose(443)
}
```

Let's enable gRPC reflection & documentation service in our local environment.

```kotlin
dockerfile {
    ...
    // Enable dev tools
    when (System.getenv("MODE")) {
        "local" -> {
            env("ENABLE_GRPC_REFLECTION", "true")
            env("ENABLE_DOC_SERVICE", "true")
        }
        else -> {}
    }
}
```
