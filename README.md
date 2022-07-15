# gradle-spectral

A Gradle plugin for running the Spectral linter against a project's OpenAPI files

## What is Spectral?

[Spectral](https://github.com/stoplightio/spectral) is an OpenAPI and AsyncAPI linter with support for custom rulesets.
Stoplight distributes the tool [in various ways](https://meta.stoplight.io/docs/spectral/docs/getting-started/2-installation.md), including as an npm package.

## What does this plugin do?

This plugin allows you to run Spectral against your project's OpenAPI files.
If you do not have Spectral installed, this plugin can automatically download it to your project directory.

## Usage

Example usage for running Spectral against the `openapi.yaml`, always downloading the latest version if a new one is released:

```kotlin
plugins {
    id("io.github.michael-nestler.spectral")
}

spectral {
    documents.from("openapi.yaml")
}
```

The `documents` are a [`ConfigurableFileCollection`](https://docs.gradle.org/current/javadoc/org/gradle/api/file/ConfigurableFileCollection.html) and can be configured as documented.

The example below illustrates all available configuration options.

```kotlin
plugins {
    id("io.github.michael-nestler.spectral")
}

spectral {
    download.set(true)                        // Default: true - set to false if Spectral is in path or locally available
    version.set("6.4.1")                      // Default: "latest" - set to a specific version to pin the downloaded Spectral version
    binary.set(file("/path/to/spectral"))     // Only used if download is set to false. If unset, uses "spectral" in path.
    
    ruleset.set(file("/path/to/ruleset.yml")) // By default, the Spectral core rulesets will be used https://github.com/stoplightio/spectral/blob/develop/docs/getting-started/3-rulesets.md#core-rulesets 
    
    documents.from("openapi.yaml")
    
    reports {
        junit {
            enabled.set(true)                 // Default: true - Set to false if you really don't want a junit xml report
            reportFile.set(file("lint.xml"))  // Default: $buildDir/test-results/spectral/spectral.xml
        }
        stylish.set(true)                     // Default: true - Set to false if you don't want console output of the result
    }
}
```

## Gradle compatibility

This plugin is only kept up-to-date with the newest version of Gradle on a personal on-demand basis.
If you need a specific version supported, I recommend you fork this repository for your organization.

## Contributing

Community contributions are welcome, but the scope of the plugin is intentionally kept narrow.
The functionality of this plugin should only be linting OpenAPI/AsyncAPI files with Spectral and reporting the results.

## Licensing

This software is licensed with the [Unlicense](https://unlicense.org). 
See [LICENSE](LICENSE) for more information.
