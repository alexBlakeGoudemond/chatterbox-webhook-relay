const {defineConfig} = require("cypress");

module.exports = defineConfig({
    e2e: {
        baseUrl: 'http://localhost:9090',
        specPattern: 'cypress/e2e/**/*.cy.js',
        video: true,                 // enable video recording
        screenshotOnRunFailure: true // optional: captures screenshots if test fails
    },
});
