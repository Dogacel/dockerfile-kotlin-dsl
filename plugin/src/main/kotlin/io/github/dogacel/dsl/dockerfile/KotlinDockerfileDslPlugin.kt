package io.github.dogacel.dsl.dockerfile

import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinDockerfileDslPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("greeting") { task ->
            task.doLast {
                println("Hello from plugin 'org.example.greeting'")
            }
        }
    }
}
