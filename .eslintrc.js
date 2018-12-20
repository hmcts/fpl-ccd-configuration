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
    "parser": "babel-eslint",
    "parserOptions": {
        "sourceType": "module"
    },
    "rules": {
        "indent": [
            "error",
            2,
            {
                "SwitchCase": 1
            }
        ],
        "linebreak-style": [
            "error",
            "unix"
        ],
        "quotes": [
            "error",
            "single"
        ],
        "no-trailing-spaces": [
            "error",
            {
                "skipBlankLines": true
            }
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
