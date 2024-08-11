package io.github.dogacel.dsl.dockerfile

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

class DockerfileDslPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("dockerfile", Dockerfile::class.java)

        project.tasks.register("dockerfileGenerate", DockerfileGenerateTask::class.java) { task ->
            task.group = "custom"
            task.dockerfile = extension
        }

        project.tasks.register("dockerfilePrint", DockerfilePrintTask::class.java) { task ->
            task.group = "custom"
            task.dockerfile = extension
        }
    }
}

abstract class DockerfileGenerateTask : DefaultTask() {
    @Input
    var dockerfile: Dockerfile? = null

    @get:Input
    @set:Option(option = "name", description = "Name of the dockerfile")
    var dockerFileName: String = "Dockerfile"

    @TaskAction
    fun executeTask() {
        val fileContent = dockerfile?.parse()

        if (fileContent == null) {
            logger.error("Dockerfile is empty.")
            return
        }

        val file =
            project.layout.projectDirectory
                .file(dockerFileName)
                .asFile

        file.createNewFile()
        file.writeText(fileContent.joinToString("\n"))

        logger.info("Generating Dockerfile")
    }
}

abstract class DockerfilePrintTask : DefaultTask() {
    @Input
    var dockerfile: Dockerfile? = null

    @TaskAction
    fun executeTask() {
        logger.info(dockerfile?.parse()?.joinToString("\n") ?: "Dockerfile is empty")
    }
}
