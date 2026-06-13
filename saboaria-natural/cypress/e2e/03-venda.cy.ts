export {};

const labelRegex = (text: string) =>
  new RegExp(text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&').replace(/\s+/g, '\\s+'));

const campo = (labelText: string) =>
  cy.contains('label', labelRegex(labelText)).parent().find('input').first();

const sel = (labelText: string) =>
  cy.contains('label', labelRegex(labelText)).parent().find('select').first();

function gerarCPF(seed: number): string {
  const base = String(seed % 1000000000).padStart(9, '0').split('').map(Number);

  let soma1 = 0;
  for (let i = 0; i < 9; i++) soma1 += base[i] * (10 - i);
  let d1 = 11 - (soma1 % 11);
  if (d1 >= 10) d1 = 0;

  let soma2 = 0;
  for (let i = 0; i < 9; i++) soma2 += base[i] * (11 - i);
  soma2 += d1 * 2;
  let d2 = 11 - (soma2 % 11);
  if (d2 >= 10) d2 = 0;

  return [...base, d1, d2].join('');
}

// Identificadores únicos por execução — nunca repetem email nem CPF
const ts = Date.now();
const email = `cypress.venda${String(ts).slice(-10)}@saboaria.com`;
const senha = 'Cypress123';
const cpf = gerarCPF(ts);
const aliasCartao1 = 'Visa Venda A';
const aliasCartao2 = 'Master Venda B';

describe('Fluxo completo de venda com dois cartões', () => {
  beforeEach(() => {
    cy.on('window:alert', () => {});
    cy.on('window:confirm', () => true);
  });

  it('cadastra novo cliente com dois cartões', () => {
    cy.visit('/cadastro');
    cy.wait(800);

    // Aba 1 — Dados Pessoais
    campo('Nome Completo').type('Cliente Venda Cypress', { force: true });
    campo('E-mail').type(email, { force: true });
    campo('Senha').type(senha, { force: true });
    campo('CPF').type(cpf, { force: true });
    campo('Data de Nascimento').type('1990-05-20', { force: true });
    sel('Gênero').select('Masculino', { force: true });
    campo('DDD').type('11', { force: true });
    campo('Número').type('912345678', { force: true });
    sel('Tipo de Telefone').select('Comercial', { force: true });
    cy.contains('button', 'Próximo: Endereços').click();

    // Aba 2 — Endereço (Av. Paulista, SP — CEP fora de Mogi das Cruzes)
    campo('CEP').type('01310100', { force: true });
    cy.wait(3000); // aguarda BrasilAPI preencher campos automaticamente
    sel('Tipo de Logradouro').select('avenida', { force: true });
    campo('Logradouro').clear().type('Paulista', { force: true });
    campo('Número').clear().type('1000', { force: true });
    campo('Bairro').clear().type('Bela Vista', { force: true });
    campo('Cidade').clear().type('São Paulo', { force: true });
    campo('Estado').clear().type('SP', { force: true });
    campo('País').clear().type('Brasil', { force: true });
    campo('Complemento').type('Sala 5', { force: true });
    sel('Tipo de Residência').select('Apartamento', { force: true });
    campo('Observações').type('Portaria comercial', { force: true });
    campo('Apelido').type('Escritório SP', { force: true });
    cy.contains('button', 'Próximo: Cartões').click();

    // Aba 3 — Cartão 1 (Visa)
    campo('Número do Cartão').type('4111111111111111', { force: true });
    campo('Nome Impresso').type('CLIENTE CYPRESS', { force: true });
    sel('Bandeira').select('visa', { force: true });
    campo('CVV').type('123', { force: true });
    cy.contains('label', labelRegex('Validade'))
      .parent()
      .find('input[type="month"]')
      .invoke('val', '2027-12')
      .trigger('input')
      .trigger('change');
    campo('Apelido do Cartão').type(aliasCartao1, { force: true });

    // Adicionar segundo cartão
    cy.contains('button', '+ Adicionar Cartão').click();

    // Cartão 2 (Mastercard) — escopado no último container de cartão da aba
    cy.get('.rounded-3xl').last().within(() => {
      cy.contains('label', /Número do\s+Cartão/)
        .parent()
        .find('input')
        .first()
        .type('5500000000000004', { force: true });
      cy.contains('label', /Nome\s+Impresso/)
        .parent()
        .find('input')
        .type('CLIENTE CYPRESS', { force: true });
      cy.contains('label', /Bandeira/)
        .parent()
        .find('select')
        .select('mastercard', { force: true });
      cy.contains('label', /CVV/)
        .parent()
        .find('input')
        .type('456', { force: true });
      cy.contains('label', /Validade/)
        .parent()
        .find('input[type="month"]')
        .invoke('val', '2028-06')
        .trigger('input')
        .trigger('change');
      cy.contains('label', /Apelido do\s+Cartão/)
        .parent()
        .find('input')
        .type(aliasCartao2, { force: true });
    });

    cy.contains('button', 'Finalizar Cadastro').click();
    cy.url().should('include', '/login');
  });

  it('realiza compra com dois cartões e endereço novo', () => {
    // Login com o cliente recém-cadastrado
    cy.visit('/login');
    cy.get('form').find('input').first().type(email);
    cy.get('form').find('input[type="password"]').type(senha);
    cy.contains('button', 'Entrar').click();
    cy.url().should('eq', Cypress.config().baseUrl + '/');

    // Adicionar "Sabonete de Lavanda" ao carrinho
    cy.intercept('GET', '**/estoques').as('carregarEstoques');
    cy.visit('/produtos');
    cy.wait('@carregarEstoques');
    cy.contains('.bg-white.border.rounded-2xl', 'Sabonete de Lavanda').find('button').click({ force: true });
    cy.wait(1000); // aguarda store.addToCart verificar estoque

    // Navegar ao checkout via Vue Router para não recarregar a página (o que limparia o carrinho)
    cy.get('header').find('a[href="/carrinho"]').click();
    cy.contains('a', 'Finalizar Compra').click();

    // Informar um CEP diferente do cadastrado para criar novo endereço (Praça da Sé, SP)
    cy.get('input[placeholder="00000-000"]').clear().type('01001000');
    cy.contains('button', 'Calcular').click();
    cy.wait(4000); // aguarda ViaCEP responder

    // Confirmar que o frete foi calculado
    cy.contains(/Frete/).should('be.visible');

    // Sabonete de Lavanda R$ 45,90 + frete R$ 15,00 = R$ 60,90 → R$ 30,45 por cartão
    // invoke('val') + trigger('input') evita o problema de locale no input[type="number"]
    // onde .type('30.45') ignora o ponto e resulta em 3045
    cy.contains('p', aliasCartao1)
      .closest('.overflow-hidden')
      .find('input[type="number"]')
      .invoke('val', '30.45')
      .trigger('input');

    cy.contains('p', aliasCartao2)
      .closest('.cursor-pointer')
      .click({ force: true });

    cy.contains('p', aliasCartao2)
      .closest('.overflow-hidden')
      .find('input[type="number"]')
      .invoke('val', '30.45')
      .trigger('input');

    // Verificar que o botão está habilitado e confirmar a compra
    cy.contains('button', /Confirmar e Pagar/).should('not.be.disabled').click();

    cy.url().should('include', '/pedidos');

    // Logout
    cy.contains('button', 'Sair').click();
    cy.url().should('include', '/login');

    // Login como admin
    cy.get('input[type="email"]').type('admin@saboaria.com');
    cy.get('input[type="password"]').type('admin');
    cy.contains('button', 'Entrar').click();
    cy.url().should('include', '/admin');

    // Navegar para vendas pelo sidebar sem recarregar a página
    cy.contains('a', 'Vendas').click();

    // Verificar que a compra do cliente aparece na listagem
    cy.contains('td', 'Cliente Venda Cypress').should('be.visible');
    cy.contains('td', 'R$ 60.90').should('be.visible');
    cy.contains('Em Processamento').should('be.visible');
  });
});
