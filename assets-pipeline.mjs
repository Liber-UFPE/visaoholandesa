import * as esbuild from "esbuild"
import {Compress} from "gzipper";
import sharp from "sharp";
import fg from "fast-glob";
import path from "path";
import * as sass from "sass";
import autoprefixer from "autoprefixer";
import postcss from "postcss";
import purgecss from "@fullhuman/postcss-purgecss"
import fs from "fs";

const resourcesFolder = "src/main/resources";
const resourcesBuildFolder = "build/resources/main"

const imagesDir = `${resourcesFolder}/public/images`
const imagesOutputDir = `${resourcesBuildFolder}/public/images`

const stylesDir = `${resourcesFolder}/public/stylesheets`
const stylesOutputDir = `${resourcesBuildFolder}/public/stylesheets`

// References:
// - https://sass-lang.com/dart-sass/#java-script-library
// - https://sass-lang.com/documentation/js-api/
async function compileBootstrap() {
    const bootstrapFile = `${resourcesFolder}/public/scss/bootstrap.scss`
    const bootstrapOutputFile = `${stylesOutputDir}/bootstrap.css`
    const result = sass.compile(bootstrapFile, {
        style: "compressed",
        loadPaths: ["node_modules"]
    });

    return await postcss([
        autoprefixer,
        purgecss({content: ["./src/**/*.kte", `${resourcesFolder}/**/*.js`]})
    ]).process(result.css, {from: undefined, map: false}).then(result => {
        result.warnings().forEach(warn => {
            console.warn(warn.toString())
        })
        writeCss(bootstrapOutputFile, result.css);
    }).then(() => bootstrapOutputFile);
}

function writeCss(destination, content) {
    fs.mkdirSync(path.dirname(destination), {recursive: true})
    fs.writeFile(destination, content, (err) => {
        if (err) {
            throw err;
        }
    });
}

async function compress(dir) {
    return await new Compress(dir, dir, {gzip: true, brotli: true, deflate: true}).run();
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
        allowOverwrite: true
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

await compileBootstrap()
    .then((bootstrapCompiledCss) => minify(bootstrapCompiledCss, stylesOutputDir))
    .then(() => minifyJS())
    .then(() => minifyCSS())
    .then(() => copyImages(imagesDir, imagesOutputDir))
    .then(() => convertToWebp(imagesOutputDir));