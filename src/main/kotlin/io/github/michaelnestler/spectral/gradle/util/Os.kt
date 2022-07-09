package io.github.michaelnestler.spectral.gradle.util

import org.gradle.nativeplatform.platform.internal.DefaultOperatingSystem
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

sealed interface Os
object Windows : Os
object Mac : Os
class Linux(val isAlpine: Boolean) : Os

fun currentOs(): Os {
    return when {
        DefaultOperatingSystem("windows").isCurrent -> Windows
        DefaultOperatingSystem("osx").isCurrent -> Mac
        DefaultOperatingSystem("linux").isCurrent -> Linux(isAlpine())
        else -> throw IllegalStateException("Unknown OS")
    }
}

private fun isAlpine(): Boolean {
    val osRelease = Path.of("/etc/os-release")
    return osRelease.isRegularFile() && osRelease.readText().contains("NAME=\"Alpine Linux\"")
}
