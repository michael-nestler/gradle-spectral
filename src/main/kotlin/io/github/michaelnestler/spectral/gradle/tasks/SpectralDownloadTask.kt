package io.github.michaelnestler.spectral.gradle.tasks

import io.github.michaelnestler.spectral.gradle.latest
import io.github.michaelnestler.spectral.gradle.normalize
import io.github.michaelnestler.spectral.gradle.util.Linux
import io.github.michaelnestler.spectral.gradle.util.Mac
import io.github.michaelnestler.spectral.gradle.util.Windows
import io.github.michaelnestler.spectral.gradle.util.currentOs
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.FileNotFoundException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect.NORMAL
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE
import kotlin.io.path.getPosixFilePermissions
import kotlin.io.path.setPosixFilePermissions

private const val windowsName = "spectral.exe"
private const val macName = "spectral-macos"
private const val linuxName = "spectral-linux"
private const val alpineName = "spectral-alpine"

abstract class SpectralDownloadTask : DefaultTask() {

    @get:Input
    abstract val version: Property<String>

    @get:OutputFile
    abstract val binary: RegularFileProperty

    private val json by lazy { Json { ignoreUnknownKeys = true } }
    private val httpClient by lazy {
        HttpClient.newBuilder().followRedirects(NORMAL).build()
    }

    init {
        outputs.upToDateWhen {
            if (version.get() == latest) {
                checkLatestUpToDate()
            } else {
                checkVersionMatches()
            }
        }
    }

    @TaskAction
    fun downloadSpectral() {
        try {
            val release = if (version.get() == latest) latestVersion() else fetchRelease(version.get())
            println("Downloading release ${release.name}")
            val versionName = versionName()
            val downloadUrl = release.assets.find { it.name == versionName }?.browser_download_url
                ?: throw IllegalStateException("No release version $versionName found")
            val downloadResponse = httpClient.send(
                HttpRequest.newBuilder(URI.create(downloadUrl)).build(),
                BodyHandlers.ofFile(binary.get().asFile.toPath())
            )
            if (currentOs() != Windows) {
                downloadResponse.body()
                    .setPosixFilePermissions(downloadResponse.body().getPosixFilePermissions() + OWNER_EXECUTE)
            }
            println("Downloaded binary to ${downloadResponse.body()}")
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun versionName(): String {
        return when (val os = currentOs()) {
            Windows -> windowsName
            Mac -> macName
            is Linux -> if (os.isAlpine) alpineName else linuxName
        }
    }

    private fun checkLatestUpToDate(): Boolean {
        println("Fetching latest version from GitHub...")
        try {
            val releaseResponse = latestVersion()
            println("Found latest version ${releaseResponse.name}")
            return checkVersionMatches(releaseResponse.name)
        } catch (exception: Exception) {
            exception.printStackTrace()
            val (returnCode, output) = try {
                probeBinary()
            } catch (fileNotFound: FileNotFoundException) {
                println("Binary cannot be verified, needs re-download: $fileNotFound")
                return false
            }
            if (returnCode != 0) {
                println("Binary returned invalid return code $returnCode. Output: $output")
                return false
            }
            println("Encountered exception while checking latest version, but spectral binary is present and should suffice")
            return true
        }
    }

    private fun checkVersionMatches(targetVersion: String = this.version.get()): Boolean {
        val version = normalize(targetVersion)
        val (returnCode, output) = try {
            probeBinary()
        } catch (_: FileNotFoundException) {
            return false
        }
        if (returnCode != 0) {
            println("Binary returned invalid return code $returnCode. Output: $output")
            return false
        }
        val binaryVersion = normalize(output)
        return if (binaryVersion == version) {
            println("Up-to-date with $version!")
            true
        } else {
            println("Not up-to-date: Found $binaryVersion instead of $version")
            false
        }
    }

    private fun latestVersion(): ReleaseResponse {
        val response = httpClient.send(
            HttpRequest.newBuilder(URI.create("https://api.github.com/repos/stoplightio/spectral/releases/latest"))
                .build(),
            BodyHandlers.ofString()
        )
        return json.decodeFromString(response.body())
    }

    private fun fetchRelease(version: String): ReleaseResponse {
        val response = httpClient.send(
            HttpRequest.newBuilder(URI.create("https://api.github.com/repos/stoplightio/spectral/releases/tags/v${normalize(version)}"))
                .build(),
            BodyHandlers.ofString()
        )
        return json.decodeFromString(response.body())
    }

    private fun probeBinary(): Pair<Int, String> {
        val binaryFile = binary.get().asFile
        if (!binaryFile.isFile) {
            throw FileNotFoundException(binaryFile.path)
        }
        val process = ProcessBuilder()
            .command(binaryFile.absolutePath, "--version")
            .start()
        val output = process.inputReader().readText()
        val returnCode = process.waitFor()
        return returnCode to output
    }
}

@Serializable
data class ReleaseResponse(val name: String, val assets: List<ReleaseAssets>)

@Serializable
data class ReleaseAssets(val name: String, val browser_download_url: String)
