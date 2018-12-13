module.exports = {
    "plugins": [
        "codeceptjs"
    ],
    "env": {
        "browser": true,
        "commonjs": true,
        "es6": true,
        "codeceptjs/codeceptjs": true,
        "node": true
    },
    "extends": "eslint:recommended",
    "parser": "babel-eslint",
    "parserOptions": {
        "sourceType": "module"
    },
    "rules": {
        "indent": [
            "error",
            2
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
