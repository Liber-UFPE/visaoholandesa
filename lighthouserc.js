"use strict"

module.exports = {
    ci: {
        collect: {
            startServerCommand: "MICRONAUT_ENVIRONMENTS=baremetal ./gradlew run",
            startServerReadyPattern: "Startup completed",
            startServerReadyTimeout: 90000, // 90 seconds. More than enough for CI.
            settings: {
                maxWaitForLoad: 10000 // 10 seconds
            },
            url: [
                "http://localhost:8080/",
                "http://localhost:8080/obras",
                "http://localhost:8080/contato",
                "http://localhost:8080/equipe",
                "http://localhost:8080/obra/1",
                "http://localhost:8080/obra/1/pagina/2",
            ]
        },
        assert: {
            preset: "lighthouse:no-pwa",
            assertions: {
                "bf-cache": "off",
                "offscreen-images": "off",
                // TODO: try to solve these later
                "heading-order": "off",
                "unused-css-rules": "off",
                "image-size-responsive": "off",
                "render-blocking-resources": "off",
                "first-contentful-paint": "off",
                "largest-contentful-paint": "off",
                // Only failing at CI:
                "tap-targets": process.env.CI === "true" ? "off" : "on"
            }
        },
        upload: {
            target: "lhci",
            serverBaseUrl: "https://liber-lighthouse-server.up.railway.app",
            token: process.env.PROJECT_TOKEN,
            githubToken: process.env.GITHUB_TOKEN,
            basicAuth: {
                username: process.env.BASIC_AUTH_USERNAME,
                password: process.env.BASIC_AUTH_PASSWORD,
            }
        }
    },
};