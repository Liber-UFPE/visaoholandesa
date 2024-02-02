FROM eclipse-temurin:21-jdk AS build

# Install Node JS
RUN apt-get update -y && apt-get install curl -y \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && node -v

COPY . /app
WORKDIR /app

# Build application
RUN npm install \
    && ./gradlew clean shadowJar -x test -x accessibilityTest --console plain --no-configuration-cache \
    && mv -vf build/libs/*.jar app.jar

FROM eclipse-temurin:21-jre-alpine

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
ENTRYPOINT [ "java", "-jar", "app.jar" ]