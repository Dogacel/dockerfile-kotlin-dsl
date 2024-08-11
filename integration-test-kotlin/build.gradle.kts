import io.github.dogacel.dsl.dockerfile.Expose.Protocol.UDP
import kotlin.time.Duration.Companion.seconds

plugins {
    kotlin("jvm") version "1.9.23"

    id("io.github.dogacel.dsl.dockerfile")
}

repositories {
    mavenCentral()
}

dockerfile {
    add {
        source = "build/libs/*.jar"
        destination = "/jars/"
        keepGitDir = true
        checksum = "sha256:1234567890"
        chown = "root:root"
        chmod = "755"
        link = true
        exclude("build/libs/*.jar")
    }

    arg("MODE")
    arg("JAR_FILE", "app.jar")

    cmd("java", "-jar", "\${JAR_FILE}")

    copy {
        source = "src/main/resources"
        destination = "/resources/"
        from = "builder"
        chown = "root:root"
        chmod = "755"
        link = true
        parents = true
        exclude("src/main/resources/*.properties")
    }

    entryPoint("java", "-jar", "\${JAR_FILE}")

    env("JAVA_OPTS", "-Xmx512m")

    expose(80)
    expose(8080, UDP)

    from("openjdk:8-jdk-alpine")
    from("openjdk:8-jdk-alpine", "linux/amd64", "builder")

    healthcheck {
        interval = 10.seconds
        timeout = 5.seconds
        startPeriod = 0.seconds
        startInterval = 2.seconds
        retries = 5
        cmd("curl", "-f", "http://localhost:8080/health")
    }

    label("foo", "bar")

    maintainer("John Doe <john@doe.com>")

    onBuild {
        add {
            source = "build/libs/*.jar"
            destination = "/app.jar"
        }
    }

    run("apk", "add", "curl")
    run("echo", "\"Hello, World!\"")

    shell("/bin/bash", "-x")

    stopSignal("SIGTERM")

    user("root")

    volume("/data", "/logs")

    workdir("/app")
}
