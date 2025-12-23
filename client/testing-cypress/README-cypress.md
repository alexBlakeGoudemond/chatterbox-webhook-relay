# README Cypress

This note summarises using Cypress as a UI Test tool

## Setup

In your codebase, suppose you have a folder structure:

```
--- client
--- api
```

Inside of the `client` directory, we are going to create a new directory: `cypress`. Inside of that new directory:

```bash

npm init -y
```

```bash
npm install cypress --save-dev
```

```bash
npx cypress open
```

If this is the first time setup of Cypress on the machine, a window will open that is similar to this: 
![cypress-open-screenshot](cypress-open-screenshot.png)

### E2E Testing

By clicking the E2E Testing section in the Cypress UI, you get some files created for free.

```bash
├─ cypress/
│  ├─ fixtures/        ← test data (we won’t need this yet)
│  ├─ support/         ← global hooks & helpers
│  │  ├─ commands.js   ← custom commands (optional)
│  │  └─ e2e.js        ← runs before every test
│  └─ e2e/             ← 👈 THIS is where tests live
├─ cypress.config.js   ← config
└─ package.json
```
