import * as esbuild from "esbuild"
import {Compress} from "gzipper";
import sharp from "sharp";
import fg from "fast-glob";
import path from "path";
import {sassPlugin} from "esbuild-sass-plugin";
import autoprefixer from "autoprefixer";
import postcss from "postcss";
import purgecss from "@fullhuman/postcss-purgecss";

const assetsFolder = "src/main/resources/public";
const assetsBuildFolder = "build/resources/main/public";

const imagesDir = `${assetsFolder}/images`;
const javascriptsDir = `${assetsFolder}/javascripts`;
const stylesDir = `${assetsFolder}/stylesheets`;

esbuild.build({
    entryPoints: [
        `${stylesDir}/main.scss`,
        `${javascriptsDir}/main.js`,
        `${imagesDir}/**/*.*`,
    ],
    bundle: true,
    legalComments: "none",
    minify: true,
    entryNames: "[dir]/[name].[hash]",
    outdir: assetsBuildFolder,
    allowOverwrite: true,
    logLevel: "info",
    loader: {
        ".webp": "copy",
        ".jpg": "copy",
        ".png": "copy",
        ".ico": "copy",
    },
    plugins: [
        sassPlugin({
            async transform(source, _resolveDir) {
                const {css} = await postcss([
                    autoprefixer,
                    purgecss({content: ["./src/**/*.kte", `${assetsFolder}/**/*.js`]})
                ]).process(source, {from: undefined, map: false});
                return css;
            }
        })
    ],
}).then(() => {
    const compressOptions = {
        brotli: true,
        deflate: true,
        deflateLevel: 9,
        gzip: true,
        gzipLevel: 9,
        exclude: ["jpeg", "jpg", "png", "ico", "webp"],
    };
    return new Compress(assetsBuildFolder, assetsBuildFolder, compressOptions).run();
}).then(() => {
    return fg
        .async([`${assetsBuildFolder}/**/*.{png,jpg}`], {caseSensitiveMatch: false, dot: true})
        .then(images =>
            images.map(image => {
                const imagePath = path.parse(image);
                const outputImage = `${imagePath.dir}/${imagePath.name}.webp`;
                return sharp(image).toFormat("webp").toFile(outputImage);
            })
        );
});