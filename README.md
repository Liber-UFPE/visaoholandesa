<!-- markdownlint-disable MD013 -->

# Project Starter

![CI Workflow](https://github.com/Liber-UFPE/project-starter/actions/workflows/build.yml/badge.svg?branch=main)
![Main Workflow](https://github.com/Liber-UFPE/project-starter/actions/workflows/main.yml/badge.svg?branch=main)

This is a project starter template. There are a few things you need to do after creating your repository using this template:

- [ ] Replace `project-starter`, `PROJECT_STARTER` (and other mentions) with your project's name
- [ ] Edit `src/main/resources/public/stylesheets/main.css` as needed (different colors, fonts, etc.)
- [ ] Edit `src/main/resources/public/javascript/main.js` as needed
- [ ] Edit `src/main/jte/layout.kte` as necessary to support your project's navigation

## Adding a new page

To add a new page, you need to edit a few files:

### 1. View

Add a new template such as `src/main/jte/my-new-page.kte` that uses the project layout:

```html
@template.layout(title = "Page title", content = @`
    @template.sections.top(title = "Main top section title", subtext = @`
        <h1>Main top section content</h1>
        <p>It is <abbr title="HyperText Markup Language">HTML</abbr>.</p>
    `)
    @template.sections.main(content = @`
        <div class="row mb-2">
            <p>Secondary section HTML</p>
        </div>
    `)
`)
```

### 2. Controller / Route

Such as `src/main/kotlin/br/ufpe/liber/controllers/IndexController.kt`, or another one if necessary:

```kotlin
@Get("/my-new-page")
fun index() = ok(templates.myNewPage()) // `myNewPage` is generated automatically
```

### 3. Layout changes

If this adds to your project's navigation, you can new links to the `navbar` in the `src/main/jte/layout.kte` file:

```diff
        <li class="nav-item">
            <a class="nav-link btn btn-outline-success" href="/" role="button">Index</a>
        </li>
+       <li class="nav-item">
+           <a class="nav-link btn btn-outline-success" href="/my-new-page" role="button">My New Page</a>
+       </li>
    </ul>
    <form class="d-flex" role="search">
        <input class="form-control me-2" type="search" placeholder="Busca" aria-label="Search">

```

### 4. Tests

Add some tests for your new page / route.

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

1. Java 21 (easier to install with [SDKMAN](https://sdkman.io/))
2. [Docker Desktop](https://www.docker.com/products/docker-desktop/) (if you want to test docker images)
3. [Ktlint CLI](https://pinterest.github.io/ktlint/1.0.0/install/cli/) (if you want to run code inspections locally)
4. [Gradle](https://gradle.org/install/#with-a-package-manager) (if you don't want to use the `./gradlew` script)
5. [chromedriver](https://chromedriver.chromium.org/downloads) (if you want to run browser tests)

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
- [Micronaut JTE Views documentation](https://micronaut-projects.github.io/micronaut-views/latest/guide/#jte)

### CI & CD

The project uses [GitHub Actions](https://docs.github.com/en/actions) to run tests, package a new version, and deploy it to [Railway.app](https://railway.app/) (experimental).

### Tests & Code Coverage

We use [Kotest](https://kotest.io/) as the test framework, and [Kover](https://github.com/Kotlin/kotlinx-kover) as the Code Coverage tool. See also [Micronaut Kotest integration docs](https://micronaut-projects.github.io/micronaut-test/latest/guide/#kotest5).

### Code Inspections

For every merge/push, and also for pull requests, there are GitHub Actions to run [ktlint](https://github.com/pinterest/ktlint) and [detekt](https://github.com/detekt/detekt). There is also an (experimental) integration with [DeepSource](https://deepsource.com/).

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
| `src/accessibilityTest`     | Root folder for accessibility test code              |
| `.github`                   | Root folder for GitHub configurations                |
| `.github/workflows`         | GitHub Actions configuration                         |

## Micronaut 4.2.0 Documentation

- [User Guide](https://docs.micronaut.io/4.2.0/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.2.0/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.2.0/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)

---

- [Shadow Gradle Plugin](https://plugins.gradle.org/plugin/com.github.johnrengelman.shadow)
- [Micronaut Gradle Plugin documentation](https://micronaut-projects.github.io/micronaut-gradle-plugin/latest/)
- [GraalVM Gradle Plugin documentation](https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html)

## Feature serialization-jackson documentation

- [Micronaut Serialization Jackson Core documentation](https://micronaut-projects.github.io/micronaut-serialization/latest/guide/)

## Feature ksp documentation

- [Micronaut Kotlin Symbol Processing (KSP) documentation](https://docs.micronaut.io/latest/guide/#kotlin)
- [https://kotlinlang.org/docs/ksp-overview.html](https://kotlinlang.org/docs/ksp-overview.html)

## Feature kotest documentation

- [Micronaut Test Kotest5 documentation](https://micronaut-projects.github.io/micronaut-test/latest/guide/#kotest5)
- [https://kotest.io/](https://kotest.io/)

## Feature micronaut-aot documentation

- [Micronaut AOT documentation](https://micronaut-projects.github.io/micronaut-aot/latest/guide/)
