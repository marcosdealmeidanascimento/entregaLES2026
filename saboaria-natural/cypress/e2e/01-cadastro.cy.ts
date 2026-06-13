const labelRegex = (text) =>
  new RegExp(text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&').replace(/\s+/g, '\\s+'));

const campo = (labelText) =>
  cy.contains('label', labelRegex(labelText)).parent().find('input').first();

const sel = (labelText) =>
  cy.contains('label', labelRegex(labelText)).parent().find('select').first();

describe('Cadastro de cliente', () => {
  beforeEach(() => {
    cy.on('window:alert', () => {});
    cy.on('window:confirm', () => true);
  });

  it('preenche as 3 abas e finaliza o cadastro', () => {
    cy.visit('/cadastro');
    cy.wait(800);

    // Aba 1: Dados Pessoais
    campo('Nome Completo').type('Maria Cypress', { force: true });
    campo('E-mail').type('cypress.maria@saboaria.com', { force: true });
    campo('Senha').type('Cypress123', { force: true });
    campo('CPF').type('12345678909', { force: true });
    campo('Data de Nascimento').type('1995-06-15', { force: true });
    sel('Gênero').select('Feminino', { force: true });
    campo('DDD').type('11', { force: true });
    campo('Número').type('912345678', { force: true });
    sel('Tipo de Telefone').select('Residencial', { force: true });

    cy.contains('button', 'Próximo: Endereços').click();

    // Aba 2: Endereço
    campo('CEP').type('01310100', { force: true });
    cy.wait(3000);
    sel('Tipo de Logradouro').select('avenida', { force: true });
    campo('Logradouro').clear().type('Paulista', { force: true });
    campo('Número').clear().type('1578', { force: true });
    campo('Bairro').clear().type('Bela Vista', { force: true });
    campo('Cidade').clear().type('São Paulo', { force: true });
    campo('Estado').clear().type('SP', { force: true });
    campo('País').clear().type('Brasil', { force: true });
    campo('Complemento').type('Apto 42', { force: true });
    sel('Tipo de Residência').select('Apartamento', { force: true });
    campo('Observações').type('Portaria 24h', { force: true });
    campo('Apelido').type('Casa SP', { force: true });

    cy.contains('button', 'Próximo: Cartões').click();

    // Aba 3: Cartão
    campo('Número do Cartão').type('4111111111111111', { force: true });
    campo('Nome Impresso').type('MARIA CYPRESS', { force: true });
    sel('Bandeira').select('visa', { force: true });
    campo('CVV').type('123', { force: true });
    cy.contains('label', labelRegex('Validade'))
      .parent()
      .find('input[type="month"]')
      .invoke('val', '2027-12')
      .trigger('input')
      .trigger('change');
    campo('Apelido do Cartão').type('Meu Visa', { force: true });

    cy.contains('button', 'Finalizar Cadastro').click();

    cy.url().should('include', '/login');
  });
});
