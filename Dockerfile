FROM ubuntu:noble AS build

# Install Node JS
RUN apt-get update -y && apt-get install curl -y \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && node -v

# Add Temurin apt repository and install Java
# Reference: https://adoptium.net/installation/linux/
RUN apt-get install -y wget apt-transport-https \
    && wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor | tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null \
    && echo "deb https://packages.adoptium.net/artifactory/deb bookworm main" | tee /etc/apt/sources.list.d/adoptium.list \
    && apt-get update -y \
    && apt-get install temurin-21-jdk -y \
    && java -version

COPY . /app
WORKDIR /app

# Build application
RUN npm install \
    && ./gradlew clean shadowJar -x test -x accessibilityTest --console plain --no-configuration-cache \
    && mv -vf build/libs/*.jar app.jar

FROM eclipse-temurin:21

ENV PEREIRA_DA_COSTA_PORT=8080
ENV MICRONAUT_ENVIRONMENTS=container

COPY --from=build /app/app.jar .
RUN useradd appuser
USER appuser
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "app.jar" ]