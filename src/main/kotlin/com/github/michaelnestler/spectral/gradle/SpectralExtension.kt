package com.github.michaelnestler.spectral.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

interface SpectralExtension {
    val download: Property<Boolean>
    val version: Property<String>
    val binary: RegularFileProperty
    val ruleset: RegularFileProperty
}
