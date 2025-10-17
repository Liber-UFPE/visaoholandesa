 # Visão Holandesa

![CI Workflow](https://github.com/Liber-UFPE/visaoholandesa/actions/workflows/build.yml/badge.svg?branch=main)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Liber-UFPE_visaoholandesa&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Liber-UFPE_visaoholandesa)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Liber-UFPE_visaoholandesa&metric=coverage)](https://sonarcloud.io/summary/new_code?id=Liber-UFPE_visaoholandesa)

A Visão Holandesa do Brasil é um projeto que disponibiliza gratuitamente as obras mais significativas produzidas pelos holandeses sobre o Brasil.

## Executar localmente

Para executar o projeto localmente, abra um terminal e execute:

```shell
./gradlew run
```

A aplicação ficará acessível em <http://localhost:8080/>.

Se você quiser recarregar a aplicação a cada alteração de código, execute [o Gradle em modo contínuo](https://docs.micronaut.io/latest/guide/index.html#gradleReload):

```shell
./gradlew run -t
```

## Requisitos

1. Java 21 (mais fácil de instalar com [SDKMAN](https://sdkman.io/))
2. [Node.js 20](https://nodejs.org/en)
3. [Docker Desktop](https://www.docker.com/products/docker-desktop/) (se você quiser testar as imagens Docker)
4. [Ktlint CLI][ktlint-cli] (se você quiser executar inspeções de código localmente)
5. [Gradle](https://gradle.org/install/#with-a-package-manager) (se você não quiser usar o script `./gradlew`)


## Aspectos técnicos

O projeto é desenvolvido usando:

- [Micronaut Framework][micronaut]
- [Gradle][gradle]
- [Kotlin][kotlin]
- [Bootstrap][bootstrap]

### Documentação de Micronaut

- [Guia do usuário](https://docs.micronaut.io/latest/guide/index.html)
- [API Referência](https://docs.micronaut.io/latest/api/index.html)
- [Referência de Configuração](https://docs.micronaut.io/latest/guide/configurationreference.html)
- [Guias sobre o Micronaut](https://guides.micronaut.io/index.html)

### Template Engine

O projeto usa JTE / KTE como template engine.

- [Documentação do JTE](https://jte.gg)
- [Tutorial JTE](https://javalin.io/tutorials/jte)

### CI & CD

O projeto usa [GitHub Actions](https://docs.github.com/en/actions) para executar testes e outras validações descritas abaixo.

#### Inspeções de código

Para cada merge/push, e também para pull requests, existem ações do GitHub para executar [ktlint][ktlint], [detekt](https://github.com/detekt), e [DiKTat](https://github.com/saveourtool/diktat) (experimental).

O ktlint está configurado para usar o estilo de código `intellij_idea` para que ele não entre em conflito com a ação de formatação de código da IntelliJ IDEA.

Há também uma integração com o Sonar Cloud: <https://sonarcloud.io/project/overview?id=Liber-UFPE_visaoholandesa>.

### Testes e Cobertura de Código

Usamos [Kotest](https://kotest.io/) como framework de teste, e [Kover](https://github.com/Kotlin/kotlinx-kover) como a ferramenta de cobertura de código. Ver também [Micronaut Kotest integrações docs](https://micronaut-projects.github.io/micronaut-test/latest/guide/index.html#kotest5).

> [!TIP]
> Veja a cobertura de código mais recente na [página do projeto no SonarCloud](https://sonarcloud.io/component_measures?metric=coverage&view=list&id=Liber-UFPE_visaoholandesa).

### Assets Pipeline

Para garantir que as páginas carreguem rapidamente, há um processamento dos assets estáticos (JavaScripts, CSS, imagens).
O [esbuild](https://esbuild.github.io/) é usado em conjunto com alguns pacotes npm:

- [sharp](https://github.com/lovell/sharp) para gerar versões `webp` das images
- [gzipper](https://github.com/gios/gzipper) para gerar versões comprimidas (`gzip`, `brotli`, `deflate`)
- [postcss](https://postcss.org/) para otimizar o uso do [Bootstrap][bootstrap] e manter apenas os estilos efetivamente usados.

Esse processamento é então integrado ao `build` principal da aplicação usando o [Gradle Plugin for Node](https://github.com/node-gradle/gradle-node-plugin).

> [!TIP]
> Dá para testar o processamento dos assets de maneira isolada executando diretamente `node assets-pipeline.mjs`.

### Layout de Diretório de Projetos

O projeto segue o padrão [Maven Standard Directory Layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html) para projetos Kotlin. As pastas principais são:

| Diretório                   | Descrição                                            |
|:----------------------------|:-----------------------------------------------------|
| `src/main`                  | Pasta raiz para código de aplicação                  |
| `src/main/jte`              | Pasta de templates JTE                               |
| `src/main/kotlin`           | Código Kotlin da aplicação                           |
| `src/main/resources`        | Configurações e outros recursos                      |
| `src/main/resources/public` | Web assets como imagens, javascript e arquivos css   |
| `src/test`                  | Pasta raiz para código de teste                      |
| `scripts`                   | Pasta com scripts para deploy usando o nginx/SystemD |
| `github`                    | Pasta raiz para configurações do GitHub              |
| `.github/workflows`         | GitHub Ações configuração                            |

[gradle]: https://gradle.org/
[kotlin]: https://kotlinlang.org/
[micronaut]: https://micronaut.io/
[bootstrap]: https://getbootstrap.com/
[ktlint]: https://github.com/pinterest/ktlint
[ktlint-cli]: https://pinterest.github.io/ktlint/latest/install/cli/
