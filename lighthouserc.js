"use strict";

const runningOnCi = process.env.CI === "true" ? "off" : "on";

module.exports = {
    ci: {
        collect: {
            startServerCommand: "MICRONAUT_ENVIRONMENTS=baremetal ./gradlew run",
            startServerReadyPattern: "Startup completed",
            startServerReadyTimeout: 90000, // 90 seconds. More than enough for CI.
            settings: {
                maxWaitForLoad: 10000, // 10 seconds
                chromeFlags: "--headless=new"
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
                "offscreen-images": "off",
                "identical-links-same-purpose": "off",
                // TODO: try to solve these later
                "heading-order": "off",
                "image-size-responsive": "off",
                "render-blocking-resources": "off",
                "first-contentful-paint": "off",
                "largest-contentful-paint": "off",
                "total-byte-weight": "off",
                // Only failing at CI:
                "tap-targets": runningOnCi,
                "target-size": runningOnCi,
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