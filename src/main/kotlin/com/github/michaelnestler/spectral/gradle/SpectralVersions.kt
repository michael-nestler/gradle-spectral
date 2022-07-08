package com.github.michaelnestler.spectral.gradle

const val latest = "latest"

fun normalize(version: String) = version.trim().removePrefix("v").trim()
