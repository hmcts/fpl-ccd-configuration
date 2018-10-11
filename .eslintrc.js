module.exports = {
    "plugins": [
        "codeceptjs"
    ],
    "env": {
        "browser": true,
        "commonjs": true,
        "es6": true,
        "codeceptjs/codeceptjs": true
    },
    "extends": "eslint:recommended",
    "parserOptions": {
        "sourceType": "module"
    },
    "rules": {
        "indent": [
            "error",
            "tab"
        ],
        "linebreak-style": [
            "error",
            "unix"
        ],
        "quotes": [
            "error",
            "single"
        ],
        "semi": [
            "error",
            "always"
        ],
		"comma-dangle": [
			"error",
			"always-multiline"
		]
    }
};
