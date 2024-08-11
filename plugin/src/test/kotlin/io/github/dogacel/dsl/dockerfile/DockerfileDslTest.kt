package io.github.dogacel.dsl.dockerfile

import io.github.dogacel.dsl.dockerfile.Expose.Protocol.UDP
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class DockerfileDslTest {
    @Test
    fun syntax() {
        val file =
            dockerfile {
                add {
                    source = "build/libs/*.jar"
                    destination = "/app.jar"
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
                    destination = "/resources"
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
                run("apk", "add", "curl", "wget")

                shell("/bin/bash", "-x")

                stopSignal("SIGTERM")

                user("root")

                volume("/data", "/logs")

                workdir("/app")
            }

        println(file.parse().joinToString("\n"))
    }

    @Test
    fun pythonSample() {
        val file =
            dockerfile {
                from("python:3.12")
                workdir("/usr/local/app")

                lines(1)
                +"Install the application dependencies"
                copy {
                    source = "requirements.txt"
                    destination = "./"
                }
                run("pip", "install", "--no-cache-dir", "-r", "requirements.txt")

                lines(1)
                +"Copy in the source code"
                copy {
                    source = "src"
                    destination = "./src"
                }
                expose(5000)

                lines(1)
                +"Setup an app user so the container doesn't run as the root user"
                run("useradd", "app")
                user("app")

                lines(1)
                cmd("uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8080")
            }

        println(file.parse().joinToString("\n"))
    }

    @Test
    fun readme() {
        dockerfile {
            from("openjdk:21-jdk-slim")
            workdir("/app")

            +"Copy the JAR file into the Docker Image"
            copy {
                source = "app.jar"
                destination = "/app/app.jar"
            }

            +"Download assets"
            val assetPath =
                when (System.getenv("MODE")) {
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

            // Enable dev tools
            when (System.getenv("MODE")) {
                "local" -> {
                    env("ENABLE_GRPC_REFLECTION", "true")
                    env("ENABLE_DOC_SERVICE", "true")
                }

                else -> {}
            }

            cmd("java", "-jar", "app.jar")
        }
    }
}
