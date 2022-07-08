package com.github.michaelnestler.spectral.gradle

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

class GradleSpectralPluginTest {
    @Test
    fun `plugin registers task`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.github.michaelnestler.spectral")

        assertNotNull(project.tasks.findByName("greeting"))
    }
}
