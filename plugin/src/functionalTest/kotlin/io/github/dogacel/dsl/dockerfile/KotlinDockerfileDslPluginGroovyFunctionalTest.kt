package io.github.dogacel.dsl.dockerfile

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class KotlinDockerfileDslPluginGroovyFunctionalTest {
    @field:TempDir
    lateinit var projectDir: File

    private val buildFile by lazy { projectDir.resolve("build.gradle") }
    private val settingsFile by lazy { projectDir.resolve("settings.gradle") }

    @Test
    fun `can run kts`() {
        // Set up the test build
        settingsFile.writeText("")
        buildFile.writeText(
            """
            plugins {
                id("io.github.dogacel.dsl.dockerfile")
            }
            
            dockerfiles {
                dockerfile {
                    from("openjdk:8-jdk-alpine")
                }
            }
            """.trimIndent(),
        )

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("dockerfileGenerate")
        runner.withProjectDir(projectDir)
        val result = runner.build()

        // Verify the result
        assertTrue(result.output.contains("FROM openjdk:8-jdk-alpine"))
    }
}
