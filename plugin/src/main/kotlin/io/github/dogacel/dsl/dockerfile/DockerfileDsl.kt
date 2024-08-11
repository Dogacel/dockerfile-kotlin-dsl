package io.github.dogacel.dsl.dockerfile

import io.github.dogacel.dsl.dockerfile.Expose.Protocol
import io.github.dogacel.dsl.dockerfile.Expose.Protocol.TCP
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@DslMarker
annotation class DockerFileDsl

@DockerFileDsl
sealed class DockerfileStep(
    val markerName: String,
) {
    abstract fun getArgs(): List<String>
}

// Based on https://docs.docker.com/engine/reference/builder

class Add : DockerfileStep("ADD") {
    var source: String? = null
    var sources: List<String> = emptyList()
    var destination: String = ""

    var keepGitDir: Boolean = false
    var checksum: String? = null
    var chown: String? = null
    var chmod: String? = null
    var link: Boolean = false

    private var exclusionList: MutableList<String> = mutableListOf()

    fun exclude(value: String) {
        exclusionList.add(value)
    }

    override fun getArgs(): List<String> {
        val list = mutableListOf<String>()

        list += listOfNotNull(source)
        list += sources

        if (keepGitDir) {
            list.add("--keep-git-dir")
        }

        if (checksum != null) {
            list.add("--checksum=$checksum")
        }

        if (chown != null) {
            list.add("--chown=$chown")
        }

        if (chmod != null) {
            list.add("--chmod=$chmod")
        }

        if (link) {
            list.add("--link")
        }

        list += destination

        return list
    }
}

class Arg : DockerfileStep("ARG") {
    var name: String = ""
    var value: String? = null

    override fun getArgs(): List<String> {
        if (value == null) {
            return listOf(name)
        }

        return listOf("$name=$value")
    }
}

class Cmd : DockerfileStep("CMD") {
    var params: List<String> = emptyList()

    override fun getArgs(): List<String> = params
}

class Copy : DockerfileStep("COPY") {
    var source: String? = null
    var sources: List<String> = emptyList()
    var destination: String = ""

    var from: String? = null
    var chown: String? = null
    var chmod: String? = null
    var link: Boolean = false
    var parents: Boolean = false

    private var exclusionList: MutableList<String> = mutableListOf()

    fun exclude(value: String) {
        exclusionList.add(value)
    }

    override fun getArgs(): List<String> {
        val list = mutableListOf<String>()

        list += listOfNotNull(source)
        list += sources

        if (from != null) {
            list.add("--from=$from")
        }

        if (chown != null) {
            list.add("--chown=$chown")
        }

        if (chmod != null) {
            list.add("--chmod=$chmod")
        }

        if (link) {
            list.add("--link")
        }

        if (parents) {
            list.add("--parents")
        }

        list += destination

        return list
    }
}

class EntryPoint : DockerfileStep("ENTRYPOINT") {
    var params: List<String> = emptyList()

    override fun getArgs(): List<String> = params
}

class Env : DockerfileStep("ENV") {
    var name: String = ""
    var value: String = ""

    override fun getArgs(): List<String> = listOf("$name=$value")
}

class Expose : DockerfileStep("EXPOSE") {
    enum class Protocol {
        TCP,
        UDP,
    }

    var port: Int = 0
    var protocol: Protocol = TCP

    override fun getArgs(): List<String> = listOf("$port/${protocol.name.lowercase(Locale.US)}")
}

class From : DockerfileStep("FROM") {
    var platform: String? = null
    var image: String = ""
    var `as`: String? = null

    override fun getArgs(): List<String> {
        val args = mutableListOf(image)

        if (platform != null) {
            args.add("--platform=$platform")
        }

        if (`as` != null) {
            args.add("AS")
            args.add(`as`!!)
        }

        return args
    }
}

class Healthcheck : DockerfileStep("HEALTHCHECK") {
    var interval: Duration = 30.seconds
    var timeout: Duration = 30.seconds
    var startPeriod: Duration = 0.seconds
    var startInterval: Duration = 5.seconds
    var retries: Int = 3

    private var __cmd = mutableListOf<String>()

    fun cmd(vararg params: String) = __cmd.addAll(params)

    override fun getArgs(): List<String> {
        val args = mutableListOf("HEALTHCHECK")

        args.add("--interval=${interval.inWholeSeconds}s")
        args.add("--timeout=${timeout.inWholeSeconds}s")
        args.add("--start-period=${startPeriod.inWholeSeconds}s")
        args.add("--start-interval=${startInterval.inWholeSeconds}s")
        args.add("--retries=$retries")

        args.addAll(__cmd)

        return args
    }
}

class Label : DockerfileStep("LABEL") {
    var labels: MutableList<Pair<String, String>> = mutableListOf()

    override fun getArgs(): List<String> = labels.map { (name, value) -> "$name=$value" }
}

class Maintainer : DockerfileStep("MAINTAINER") {
    var name: String = ""

    override fun getArgs(): List<String> = listOf(name)
}

class Run : DockerfileStep("RUN") {
    var commands: List<String> = emptyList()
    // TODO: Options

    override fun getArgs(): List<String> = commands
}

class Shell : DockerfileStep("SHELL") {
    var commands: List<String> = emptyList()

    override fun getArgs(): List<String> = commands
}

class StopSignal : DockerfileStep("STOPSIGNAL") {
    var value: String = ""

    override fun getArgs(): List<String> = listOf(value)
}

class User : DockerfileStep("USER") {
    var value: String = ""

    override fun getArgs(): List<String> = listOf(value)
}

class Volume : DockerfileStep("VOLUME") {
    var volumes: List<String> = emptyList()

    override fun getArgs(): List<String> = volumes
}

class Workdir : DockerfileStep("WORKDIR") {
    var path: String = ""

    override fun getArgs(): List<String> = listOf(path)
}

// Extensions

class Comment : DockerfileStep("#") {
    var comment: String = ""

    override fun getArgs(): List<String> = listOf(comment)
}

class Space : DockerfileStep("") {
    override fun getArgs(): List<String> = emptyList()
}

@DockerFileDsl
class Dockerfile {
    private var steps: MutableList<DockerfileStep> = mutableListOf()

    // Begin base commands

    fun add(init: Add.() -> Unit) = steps.add(Add().apply(init))

    infix fun arg(name: String) = steps.add(Arg().apply { this.name = name })

    fun arg(
        name: String,
        value: String,
    ) = steps.add(
        Arg().apply {
            this.name = name
            this.value = value
        },
    )

    fun cmd(vararg params: String) = steps.add(Cmd().apply { this.params = params.toList() })

    fun copy(init: Copy.() -> Unit) = steps.add(Copy().apply(init))

    fun entryPoint(vararg params: String) = steps.add(EntryPoint().apply { this.params = params.toList() })

    fun env(
        name: String,
        value: String,
    ) = steps.add(
        Env().apply {
            this.name = name
            this.value = value
        },
    )

    fun expose(
        port: Int,
        protocol: Protocol = TCP,
    ) = steps.add(
        Expose().apply {
            this.port = port
            this.protocol = protocol
        },
    )

    fun from(
        image: String,
        platform: String? = null,
        `as`: String? = null,
    ) = steps.add(
        From().apply {
            this.image = image
            this.platform = platform
            this.`as` = `as`
        },
    )

    fun healthcheck(init: Healthcheck.() -> Unit) = steps.add(Healthcheck().apply(init))

    fun label(
        name: String,
        value: String,
    ) = steps.add(Label().apply { labels.add(name to value) })

    fun maintainer(value: String) = steps.add(Maintainer().apply { this.name = value })

    fun onBuild(init: Dockerfile.() -> Unit) {}

    fun run(vararg commands: String) = steps.add(Run().apply { this.commands = commands.toList() })

    fun shell(vararg commands: String) = steps.add(Shell().apply { this.commands = commands.toList() })

    fun stopSignal(value: String) = steps.add(StopSignal().apply { this.value = value })

    fun user(value: String) = steps.add(User().apply { this.value = value })

    fun volume(vararg volumes: String) = steps.add(Volume().apply { this.volumes = volumes.toList() })

    fun workdir(value: String) = steps.add(Workdir().apply { this.path = value })

    // End base commands
    // Begin extensions

    // Add comments
    operator fun String.unaryPlus() {
        steps.add(Comment().apply { this.comment = this@unaryPlus })
    }

    // Add spaces
    fun lines(count: Int) {
        repeat(count) {
            steps.add(Space())
        }
    }
    // End extensions

    fun parse(): List<String> =
        steps.map {
            "${it.markerName} ${it.getArgs().joinToString(" ")}"
        }
}

fun dockerfile(init: Dockerfile.() -> Unit) = Dockerfile().apply(init)
