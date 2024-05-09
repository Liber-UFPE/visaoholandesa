// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.

import globals from "globals";
import js from "@eslint/js";
import stylisticJs from "@stylistic/eslint-plugin-js";


export default [
    js.configs.recommended,
    {
        plugins: {
            "@stylistic/js": stylisticJs
        },
        languageOptions: {
            ecmaVersion: 2022,
            sourceType: "module",
            globals: {
                ...globals.browser,
                module: "writable",
                process: "readonly",
                require: "readonly",
            }
        },
        ignores: ["node_modules/*", "build/*"],
        rules: {
            "@stylistic/js/indent": [
                "error",
                4
            ],
            "@stylistic/js/linebreak-style": [
                "error",
                "unix"
            ],
            "@stylistic/js/quotes": [
                "error",
                "double"
            ],
            "@stylistic/js/semi": [
                "error",
                "always"
            ]
        }
    }
];
