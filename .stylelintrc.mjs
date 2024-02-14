// DO NOT EDIT: this file is automatically synced from the template repository
// in https://github.com/Liber-UFPE/project-starter.
/** @type {import('stylelint').Config} */
export default {
  extends: "stylelint-config-standard-scss",
  rules: {
    "no-descending-specificity": null,
    "scss/at-rule-no-unknown": [
      true,
      {
        "ignoreAtRules": [
          "tailwind",
          "screen"
        ]
      }
    ]
  }
};
