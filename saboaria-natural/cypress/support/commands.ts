Cypress.Commands.add('login', (email, senha) => {
  cy.visit('/login');
  cy.get('form').find('input').first().type(email);
  cy.get('form').find('input[type="password"]').type(senha);
  cy.contains('button', 'Entrar').click();
  cy.url().should('eq', Cypress.config().baseUrl + '/');
});
