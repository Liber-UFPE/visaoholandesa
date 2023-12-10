# Visão Holandesa

![CI Workflow](https://github.com/Liber-UFPE/visaoholandesa/actions/workflows/build.yml/badge.svg?branch=main)

The Dutch Vision of Brazil (Visão Holandesa do Brasil) is a project that makes available for free access the most significant works produced by the Dutch about Brazil.

## Run locally

To run the project locally, open a terminal and execute:

```shell
./gradlew run
```

If you want to reload the application for every code change, run [Gradle in _continuous_ mode](https://docs.micronaut.io/latest/guide/#gradleReload):

```shell
./gradlew run -t
```

## Requirements

1. Java 17+ (easier to install with [SDKMAN](https://sdkman.io/))
2. [Docker Desktop](https://www.docker.com/products/docker-desktop/) (if you want to test docker images)
3. [Ktlint CLI](https://pinterest.github.io/ktlint/1.0.0/install/cli/) (if you want to run code inspections locally)
4. [Gradle](https://gradle.org/install/#with-a-package-manager) (if you don't want to use the `./gradlew` script)

## Technical aspects

The project is developed using Micronaut Framework, [Gradle](https://gradle.org/), and [Kotlin](https://kotlinlang.org/).

### Micronaut Documentation

- [User Guide](https://docs.micronaut.io/4.1.3/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.1.3/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.1.3/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)

### Template Engine

It uses JTE/KTE as the template engine.

- [JTE Website](https://jte.gg/)
- [JTE Documentation](https://github.com/casid/jte/blob/main/DOCUMENTATION.md)
- [JTE Tutorial](https://javalin.io/tutorials/jte)

### CI & CD

The project uses [GitHub Actions](https://docs.github.com/en/actions) to run tests, package a new version, and create a release for every merge/push made to `main` branch.

### Tests & Code Coverage

We use [Kotest](https://kotest.io/) as the test framework, and [Kover](https://github.com/Kotlin/kotlinx-kover) as the Code Coverage tool. See also [Micronaut Kotest integration docs].(https://micronaut-projects.github.io/micronaut-test/latest/guide/#kotest5)

### Code Inspections

For every merge/push, and also for pull requests, there are GitHub Actions to run [ktlint](https://github.com/pinterest/ktlint) and [detekt](https://github.com/detekt/detekt). 

Ktlint is configured to use `intellij_idea` code style so that it won't conflict with code formatting action in IDEA.

### Project Directory Layout

Project follow the default [Maven Standard Directory Layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html) for Kotlin projects. The main folders are:

| Directory                   | Description                                          |
|:----------------------------|:-----------------------------------------------------|
| `src/main`                  | Root folder for application code                     |
| `src/main/jte`              | JTE template folder                                  |
| `src/main/kotlin`           | Application Kotlin code                              |
| `src/main/resources`        | Configurations and other resources                   |
| `src/main/resources/public` | Web assets such as images, javascript, and css files |
| `src/test`                  | Root folder for test code                            |
| `.github`                   | Root folder for GitHub configurations                |
| `.github/workflows`         | GitHub Actions configuration                         |
