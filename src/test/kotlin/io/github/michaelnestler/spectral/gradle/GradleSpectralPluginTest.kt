package io.github.michaelnestler.spectral.gradle

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

class GradleSpectralPluginTest {
    @Test
    fun `plugin registers task`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.michael-nestler.spectral")

        assertNotNull(project.tasks.findByName("spectral"))
    }
}
