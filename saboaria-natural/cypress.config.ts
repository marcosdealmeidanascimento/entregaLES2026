import { defineConfig } from 'cypress';

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:3000',
    viewportWidth: 1280,
    viewportHeight: 800,
    defaultCommandTimeout: 10000,
    responseTimeout: 30000,
    requestTimeout: 30000,
  },
  env: {
    clienteEmail: 'cypress.maria@saboaria.com',
    clienteSenha: 'Cypress123',
  },
});
