package io.github.dogacel.dsl.dockerfile

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

class DockerfileDslPluginTest {
    @Test
    fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.dogacel.dsl.dockerfile")

        // Verify the result
        assertNotNull(project.tasks.findByName("dockerfileGenerate"))
    }
}
