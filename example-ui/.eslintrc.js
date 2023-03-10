module.exports = {
    root: true,
    env: {
        browser: true,
        es2021: true,
        node: true
    },
    extends: [
        'plugin:vue/vue3-recommended',
        'eslint:recommended',
        '@vue/typescript/recommended'
    ],
    parserOptions: {
        ecmaVersion: 2021
    },
    plugins: [
        'vue',
        '@typescript-eslint'
    ],
    rules: {
        'vue/no-multiple-template-root': 'off',
        'vue/html-self-closing': [ 'warn', {
            'html': {
                'void': 'always'
            }
        } ],
        'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
        'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
        semi: [ 'error', 'always' ],
        indent: [ 'warn', 4 ],
        'array-bracket-spacing': [ 'warn', 'always' ],
        'space-before-function-paren': [ 'warn', {
            anonymous: 'always',
            named: 'never',
            asyncArrow: 'always'
        } ],
        curly: [ 'warn', 'multi-or-nest', 'consistent' ],
        'brace-style': [ 'warn', 'stroustrup' ],
        'vue/html-indent': [ 'warn', 4 ],
        'no-empty-function': 'off',
        '@typescript-eslint/no-empty-function': [ "error", { "allow": [ "private-constructors" ] } ],
        'vue/multi-word-component-names': 'off'
    }
};
