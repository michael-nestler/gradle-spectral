plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("com.gradle.plugin-publish") version "1.0.0"
    id("fr.brouillard.oss.gradle.jgitver") version "0.9.1"
    id("com.diffplug.spotless") version "6.8.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()

            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-junit5")
            }
        }

        val functionalTest by registering(JvmTestSuite::class) {
            useJUnitJupiter()

            dependencies {
                implementation(project)
                implementation("org.jetbrains.kotlin:kotlin-test-junit5")
            }

            targets {
                all {
                    testTask.configure {
                        useJUnitPlatform {
                            if (System.getenv("PATH_TO_SPECTRAL") == null) {
                                excludeTags("local")
                            }
                            if (System.getenv("RUN_PATH_TEST") == null) {
                                excludeTags("path")
                            }
                        }
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

pluginBundle {
    website = "https://github.com/michael-nestler/gradle-spectral"
    vcsUrl = "https://github.com/michael-nestler/gradle-spectral.git"
    tags = listOf("openapi", "swagger", "lint")
}

gradlePlugin {
    val spectral by plugins.creating {
        id = "io.github.michael-nestler.spectral"
        displayName = "Gradle Spectral"
        description = "Lint your OpenAPI docs with Spectral"
        implementationClass = "io.github.michaelnestler.spectral.gradle.GradleSpectralPlugin"
    }
}

jgitver {
    nonQualifierBranches = "main"
}

gradlePlugin.testSourceSets(sourceSets["functionalTest"])

spotless {
    kotlin {
        ktlint()
    }
    kotlinGradle {
        ktlint()
    }
}

tasks.check {
    dependsOn(testing.suites.named("functionalTest"))
}

group = "io.github.michael-nestler"
