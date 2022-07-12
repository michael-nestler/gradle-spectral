package io.github.michaelnestler.spectral.gradle

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class SpectralExtension @Inject constructor(objects: ObjectFactory) {
    abstract val download: Property<Boolean>
    abstract val version: Property<String>
    abstract val binary: RegularFileProperty
    abstract val ruleset: RegularFileProperty
    abstract val documents: ConfigurableFileCollection
    val reports: SpectralReportsExtension = objects.newInstance(SpectralReportsExtension::class.java)

    fun reports(block: SpectralReportsExtension.() -> Unit) = reports.apply(block)
}

abstract class SpectralReportsExtension @Inject constructor(objects: ObjectFactory) {
    val junit: SpectralJUnitConfig = objects.newInstance(SpectralJUnitConfig::class.java)
    abstract val stylish: Property<Boolean>

    fun junit(block: SpectralJUnitConfig.() -> Unit) = junit.apply(block)
}

interface SpectralJUnitConfig {
    val enabled: Property<Boolean>
    val reportFile: RegularFileProperty
}
