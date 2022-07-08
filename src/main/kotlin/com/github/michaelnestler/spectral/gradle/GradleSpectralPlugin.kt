package com.github.michaelnestler.spectral.gradle

import com.github.michaelnestler.spectral.gradle.tasks.SpectralDownloadTask
import com.github.michaelnestler.spectral.gradle.util.Windows
import com.github.michaelnestler.spectral.gradle.util.currentOs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.tasks.Exec
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.writeText

class GradleSpectralPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("spectral", SpectralExtension::class.java)
        extension.download.convention(true)
        extension.version.convention(latest)

        val spectralDownload = project.tasks.register("spectralDownload", SpectralDownloadTask::class.java) { task ->
            task.group = "verification"
            task.version.set(extension.version)
            task.binary.convention(project.layout.projectDirectory.file(".gradle/spectral/spectral${suffix()}"))
        }

        project.tasks.register("spectral", Exec::class.java) { task ->
            if (extension.download.get()) {
                task.dependsOn(spectralDownload)
                task.executable(spectralDownload.get().binary.get().asFile.absolutePath)
            } else {
                val binary: RegularFile? = extension.binary.orNull
                if (binary == null) {
                    task.executable("spectral")
                } else {
                    val binaryFile = binary.asFile
                    if (!binaryFile.isFile) {
                        throw IllegalArgumentException("Not a file: $binaryFile")
                    }
                    task.executable(binaryFile.absolutePath)
                }
            }
            val ruleset = extension.ruleset.orNull
            val rulesetPath = if (ruleset == null) {
                val config = Files.createTempFile("spectral-config", ".json")
                config.toFile().deleteOnExit()
                task.doFirst {
                    config.writeText("{\"extends\": [\"spectral:oas\", \"spectral:asyncapi\"]\n}\n")
                }
                config.absolutePathString()
            } else {
                if (!ruleset.asFile.isFile) {
                    throw IllegalArgumentException("Not a file: $ruleset")
                }
                ruleset.asFile.absolutePath
            }
            task.args("lint", "openapi.yaml", "--ruleset", rulesetPath)
        }
    }

    private fun suffix(): String {
        return if (currentOs() == Windows) ".exe" else ""
    }
}
