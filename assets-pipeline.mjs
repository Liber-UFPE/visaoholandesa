import * as esbuild from "esbuild"
import {Compress} from "gzipper";
import sharp from "sharp";
import fg from "fast-glob";
import path from "path";
import {sassPlugin} from "esbuild-sass-plugin";
import autoprefixer from "autoprefixer";
import postcss from "postcss";
import purgecss from "@fullhuman/postcss-purgecss"

const resourcesFolder = "src/main/resources";
const resourcesBuildFolder = "build/resources/main"

const imagesDir = `${resourcesFolder}/public/images`
const imagesOutputDir = `${resourcesBuildFolder}/public/images`

const stylesDir = `${resourcesFolder}/public/stylesheets`
const stylesOutputDir = `${resourcesBuildFolder}/public/stylesheets`

// References:
// - https://esbuild.github.io/plugins/
// - https://www.npmjs.com/package/esbuild-sass-plugin
async function bundleBootstrap() {
    const bootstrapFile = `${resourcesFolder}/public/scss/bootstrap.scss`;
    const outputDir = stylesOutputDir;

    return await esbuild.build({
        entryPoints: [bootstrapFile],
        bundle: true,
        legalComments: "none",
        minify: true,
        entryNames: "[name].[hash]",
        outdir: outputDir,
        allowOverwrite: true,
        logLevel: "info",
        plugins: [
            sassPlugin({
                async transform(source, resolveDir) {
                    const {css} = await postcss([
                        autoprefixer,
                        purgecss({content: ["./src/**/*.kte", `${resourcesFolder}/**/*.js`]})
                    ]).process(source)
                    return css
                }
            })
        ],
    }).then(() => compress(outputDir));
}

async function compress(dir) {
    const compressOptions = {
        brotli: true,
        deflate: true,
        deflateLevel: 9,
        gzip: true,
        gzipLevel: 9,
        verbose: true,
    }
    return await new Compress(dir, dir, compressOptions).run();
}

async function minify(entryPoint, outputDir) {
    return await esbuild.build({
        entryPoints: [entryPoint],
        bundle: true,
        legalComments: "none",
        minify: true,
        entryNames: "[name].[hash]",
        outdir: outputDir,
        allowOverwrite: true,
        logLevel: "info",
    }).then(() => compress(outputDir));
}

async function copyImages(sourceDir, outputDir) {
    return await esbuild.build({
        entryPoints: [`${sourceDir}/**/*.*`],
        entryNames: "[dir]/[name].[hash]",
        outdir: outputDir,
        loader: {
            ".webp": "copy",
            ".jpg": "copy",
            ".png": "copy",
            ".ico": "copy",
        },
        allowOverwrite: true,
        logLevel: "info",
    });
}

async function minifyJS() {
    return await minify(
        `${resourcesFolder}/public/javascripts/main.js`,
        `${resourcesBuildFolder}/public/javascripts`
    );
}

async function minifyCSS() {
    return await minify(
        `${stylesDir}/main.css`,
        stylesOutputDir
    );
}

async function convertToWebp(imagesDir) {
    const images = await fg.async([`${imagesDir}/*.{png,jpg}`], {caseSensitiveMatch: false, dot: true});

    images.forEach(image => {
        const imagePath = path.parse(image);
        const outputImage = `${imagePath.dir}/${imagePath.name}.webp`
        sharp(image).toFormat("webp").toFile(outputImage)
    });
}

await bundleBootstrap()
    .then(() => minifyJS())
    .then(() => minifyCSS())
    .then(() => copyImages(imagesDir, imagesOutputDir))
    .then(() => convertToWebp(imagesOutputDir));