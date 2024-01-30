package io.github.michaelnestler.spectral.gradle

const val LATEST = "latest"

fun normalize(version: String) = version.trim().removePrefix("v").trim()
