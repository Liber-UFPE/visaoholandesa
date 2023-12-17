import * as esbuild from "esbuild"
import {Compress} from "gzipper";
import sharp from "sharp";
import fg from "fast-glob";
import path from "path";

const resourcesFolder = "src/main/resources";
const resourcesBuildFolder = "build/resources/main"

async function compress(dir) {
    await new Compress(dir, dir, {gzip: true, brotli: true, deflate: true}).run();
}

async function minify(entryPoint, outputDir) {
    await esbuild.build({
        entryPoints: [entryPoint],
        minify: true,
        entryNames: "[name].[hash]",
        outdir: outputDir,
        allowOverwrite: true,
    }).then(() => compress(outputDir));
}

await minify(
    `${resourcesFolder}/public/javascripts/main.js`,
    `${resourcesBuildFolder}/public/javascripts`
)

await minify(
    `${resourcesFolder}/public/stylesheets/main.css`,
    `${resourcesBuildFolder}/public/stylesheets`
)

const imagesDir = `${resourcesFolder}/public/images`
const imagesOutputDir = `${resourcesBuildFolder}/public/images`

async function convertToWebp() {
    const images = await fg.async([`${imagesOutputDir}/*.{png,jpg}`], {caseSensitiveMatch: false, dot: true});

    images.forEach(image => {
        const imagePath = path.parse(image);
        const outputImage = `${imagePath.dir}/${imagePath.name}.webp`
        sharp(image).toFormat("webp").toFile(outputImage)
    });
}

await esbuild.build({
    entryPoints: [`${imagesDir}/**/*.*`],
    entryNames: "[dir]/[name].[hash]",
    outdir: imagesOutputDir,
    loader: {
        ".webp": "copy",
        ".jpg": "copy",
        ".png": "copy",
        ".ico": "copy",
    },
    allowOverwrite: true
}).then(() => convertToWebp());