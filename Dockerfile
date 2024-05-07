FROM eclipse-temurin:21.0.3_9-jdk-jammy AS build

# Install Node JS
RUN apt-get update -y && apt-get install --no-install-recommends -y curl git \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y --no-install-recommends nodejs \
    && node -v

# Better layer caching by installing dependencies first
WORKDIR /app
COPY package.json yarn.lock /app/
COPY settings.gradle.kts build.gradle.kts gradlew gradle.properties /app/
COPY gradle /app/gradle
COPY buildSrc /app/buildSrc

# Creates the jte directory to avoid errors with jte plugin
COPY src/main/jte/.jteroot /app/src/main/jte/.jteroot

RUN corepack enable && \
    yarn install && \
    ./gradlew -Dsonar.gradle.skipCompile=true --console plain --no-configuration-cache classes -x assetsPipeline

# Build application
COPY . /app/
RUN ./gradlew -Dsonar.gradle.skipCompile=true --console plain --no-configuration-cache \
      clean shadowJar -x test -x accessibilityTest \
      && mv -vf build/libs/*.jar app.jar

FROM eclipse-temurin:21.0.3_9-jre-alpine

LABEL org.opencontainers.image.description="Visao Holandesa Java Application Service"
LABEL org.opencontainers.image.url="https://github.com/Liber-UFPE/visaoholandesa/"
LABEL org.opencontainers.image.documentation="https://github.com/Liber-UFPE/visaoholandesa/"
LABEL org.opencontainers.image.source="https://github.com/Liber-UFPE/visaoholandesa/"
LABEL org.opencontainers.image.vendor="Laboratório Liber / UFPE"
LABEL org.opencontainers.image.licenses="Apache-2.0"
LABEL org.opencontainers.image.title="Visao Holandesa"

ENV VISAO_HOLANDESA_PORT=8080
ENV MICRONAUT_ENVIRONMENTS=container

COPY --from=build /app/app.jar .
EXPOSE 8080
HEALTHCHECK CMD curl -f "http://localhost:$VISAO_HOLANDESA_PORT/" || exit 1

# Add unprivileged user to run the service:
# https://docs.docker.com/engine/reference/builder/#user
# https://docs.docker.com/develop/develop-images/instructions/#user
# https://manpages.ubuntu.com/manpages/noble/en/man8/addgroup.8.html
# https://manpages.ubuntu.com/manpages/noble/en/man8/adduser.8.html
RUN addgroup --system visaoholandesa && adduser --system -G visaoholandesa visaoholandesa
USER visaoholandesa

ENTRYPOINT [ "java", "-jar", "app.jar" ]