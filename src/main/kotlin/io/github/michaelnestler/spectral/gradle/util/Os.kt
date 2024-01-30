package io.github.michaelnestler.spectral.gradle.util

import org.gradle.nativeplatform.platform.internal.DefaultOperatingSystem
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

sealed interface Os

object Windows : Os

object Mac : Os

class Linux(val isAlpine: Boolean) : Os

enum class Architecture {
    X64,
    ARM64,
    UNKNOWN,
}

fun currentOs(): Os {
    return when {
        DefaultOperatingSystem("windows").isCurrent -> Windows
        DefaultOperatingSystem("osx").isCurrent -> Mac
        DefaultOperatingSystem("linux").isCurrent -> Linux(isAlpine())
        else -> throw IllegalStateException("Unknown OS")
    }
}

fun currentArchitecture(): Architecture {
    val arch = System.getProperty("os.arch")
    return when {
        arch.equals("arm") || arch.equals("aarch64") -> Architecture.ARM64
        arch.contains("64") -> Architecture.X64
        else -> Architecture.UNKNOWN
    }
}

private fun isAlpine(): Boolean {
    val osRelease = Path.of("/etc/os-release")
    return osRelease.isRegularFile() && osRelease.readText().contains("NAME=\"Alpine Linux\"")
}
