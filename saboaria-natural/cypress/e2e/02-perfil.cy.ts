const labelRegex = (text) =>
  new RegExp(text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&').replace(/\s+/g, '\\s+'));

const campo = (labelText) =>
  cy.contains('label', labelRegex(labelText)).parent().find('input').first();

describe('Login e atualização de perfil', () => {
  beforeEach(() => {
    cy.on('window:alert', () => {});
    cy.on('window:confirm', () => true);
  });

  it('faz login com o cliente cadastrado', () => {
    cy.visit('/login');
    cy.get('form').find('input').first().type('cypress.maria@saboaria.com');
    cy.get('form').find('input[type="password"]').type('Cypress123');
    cy.contains('button', 'Entrar').click();
    cy.url().should('eq', Cypress.config().baseUrl + '/');
  });

  it('atualiza todos os dados no perfil', () => {
    cy.visit('/login');
    cy.get('form').find('input').first().type('cypress.maria@saboaria.com');
    cy.get('form').find('input[type="password"]').type('Cypress123');
    cy.contains('button', 'Entrar').click();
    cy.url().should('eq', Cypress.config().baseUrl + '/');

    cy.visit('/perfil');
    cy.contains('label', labelRegex('Nome Completo')).should('be.visible');
    cy.wait(800);

    // Aba 1: Dados Pessoais
    campo('Nome Completo').clear().type('Maria Cypress Atualizada', { force: true });

    cy.contains('button', 'Próximo: Endereços').click();

    // Aba 2: Endereço
    campo('Número').clear().type('2000', { force: true });
    campo('Complemento').clear().type('Apto 99', { force: true });

    cy.contains('button', 'Próximo: Cartões').click();

    // Aba 3: Cartão
    campo('Nome Impresso').clear().type('MARIA C ATUALIZADA', { force: true });
    campo('Apelido do Cartão').clear().type('Visa Principal', { force: true });

    cy.window().then((win) => {
      cy.stub(win, 'alert').as('alertStub');
    });

    cy.contains('button', 'Atualizar Cadastro').click();

    cy.get('@alertStub').should('have.been.calledWith', 'Cadastro atualizado com sucesso!');
  });
});
