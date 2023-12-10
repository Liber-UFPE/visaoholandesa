 # Visão Holandesa

![CI Workflow](https://github.com/Liber-UFPE/visaoholandesa/actions/workflows/build.yml/badge.svg?branch=main)

A Visão Holandesa do Brasil é um projeto que disponibiliza gratuitamente as obras mais significativas produzidas pelos holandeses sobre o Brasil.

## Executar localmente

Para executar o projeto localmente, abra um terminal e execute:

```shell
./gradlew run
```

A aplicação ficará acessível em <http://localhost:8080/>.

Se você quiser recarregar a aplicação a cada alteração de código, execute [o Gradle em modo contínuo](https://docs.micronaut.io/latest/latest/#gradleReload):

```shell
./gradlew run -t
```

## Simular ambiente de produção

O aplicativo é executado usando o [nginx](https://nginx.org/) como proxy, em uma máquina com o [Rocky Linux](https://rockylinux.org/). Para simular este ambiente, você pode usar o [Vagrant](https://www.vagrantup.com/), que irá configurar todos os detalhes usando um único comando:

```shell
./gradlew clean vagrantUp
```

O servidor nginx ficará acessível em <http://localhost:9080/>, e a aplicação em <http://localhost:9080/visaoholandesa>.

Para acessar a VM via SSH, execute:

```shell
vagrant ssh
```

### Destruir / Reiniciar a VM Vagrant

Se a VM estiver executando, execute o seguinte comando para destruí-lo:

```shell
vagrant destroy --graceful --force
```

## Requisitos

1. Java 17+ (mais fácil de instalar com [SDKMAN](https://sdkman.io/))
2. [Docker Desktop](https://www.docker.com/products/docker-desktop/) (se você quiser testar as imagens Docker)
3. [Ktlint CLI](https://pinterest.github.io/ktlint/1.0.0/install/cli/) (se você quiser executar inspeções de código localmente)
4. [Gradle](https://gradle.org/install/-with-a-package-manager) (se você não quiser usar o script `./gradlew`)
5. [Vagrant](https://www.vagrantup.com/) (se você quiser rodar o projeto usando uma VM)


## Aspectos técnicos

O projeto é desenvolvido usando Micronaut Framework, [Gradle](https://gradle.org/), e [Kotlin](https://kotlinlang.org/).

### Documentação de Micronaut

- [Guia do usuário](https://docs.micronaut.io/4.1.3/guida/index.html)
- [API Referência](https://docs.micronaut.io/4.1.3/api/index.html)
- [Referência de Configuração](https://docs.micronaut.io/4.1.3/guide/configurationreference.html)
- [Guias sobre o Micronaut](https://guides.micronaut.io/index.html)

### Template Engine

O projeto usa JTE / KTE como template engine.

- [Web Website](https://jte.gg/)
- [Documentação do JTE](https://regthub.com/casid/jte/blob/main/DOCUMENTATION.md)
- [Tutorial JTE](https://javalin.io/tutorials/jte)

### CI & CD

O projeto usa [GitHub Actions](https://docs.github.com/en/actions) para executar testes, empacotar uma nova versão, e criar uma versão para cada merge/push feito para o branch `main`.

### Testes e Cobertura de Código

Usamos [Kotest](https://kotest.io/) como estrutura de teste, e [Kover](https://github.com/Kotlin/kotlinx-kover) como a ferramenta Cobertura de Código. Ver também[Micronaut Kotest integrações docs](https://micronaut-projects.github.io/micronaut-test/latest/latest/guide/?kotest5).

### Inspeções de código

Para cada merge/push, e também para pull requests, existem ações do GitHub para executar [ktlint](https://github.com/pinterest/ktlint) e [detekt](https://github.com/detekt).

O Ktlint está configurado para usar o estilo de código `intellij_idea` para que ele não entre em conflito com a ação de formatação de código da IntelliJ IDEA.

### Layout de Diretório de Projetos

O projeto segue o padrão [Maven Standard Directory Layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html) para projetos Kotlin. As pastas principais são:

| Diretório                   | Descrição                                          |
|:----------------------------|:---------------------------------------------------|
| `src/main`                  | Pasta raiz para código de aplicação                |
| `src/main/jte`              | Pasta de templates JTE                             |
| `src/main/kotlin`           | Código Kotlin da aplicação                         |
| `src/main/resources`        | Configurações e outros recursos                    |
| `src/main/resources/public` | Web assets como imagens, javascript e arquivos css |
| `src/test`                  | Pasta raiz para código de teste                    |
| `scripts`                   | Pasta com scripts para deploy usando o Vagrant     |
| `github`                    | Pasta raiz para configurações do GitHub            |
| `.github/workflows`         | GitHub Ações configuração                          |
