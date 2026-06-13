export {};

// ─── Helpers (idênticos ao fluxo-mestre) ─────────────────────────────────────

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
  let d1 = 11 - (soma1 % 11); if (d1 >= 10) d1 = 0;
  let soma2 = 0;
  for (let i = 0; i < 9; i++) soma2 += base[i] * (11 - i);
  soma2 += d1 * 2;
  let d2 = 11 - (soma2 % 11); if (d2 >= 10) d2 = 0;
  return [...base, d1, d2].join('');
}

// ─── Dados únicos por execução ────────────────────────────────────────────────

const ts          = Date.now();
const email       = `loop.compras${String(ts).slice(-10)}@saboaria.com`;
const senha       = 'Cypress123';
const cpf         = gerarCPF(ts);
const nomeCliente = `Loop Compras ${String(ts).slice(-6)}`;
const aliasCartao = 'Visa Loop';

const PRODUTOS = [
  'Sabonete de Lavanda',
  'Sabonete de Capim-Limão',
  'Sabonete de Erva-Doce',
  'Sabonete de Alecrim',
  'Sabonete de Camomila',
  'Sabonete de Rosa',
  'Sabonete de Jasmim',
  'Sabonete de Limão Siciliano',
  'Sabonete de Laranja e Baunilha',
  'Sabonete de Argila Verde',
];

// ─── Login helpers ────────────────────────────────────────────────────────────

const loginCliente = () => {
  cy.visit('/login');
  cy.get('form').find('input').first().type(email);
  cy.get('form').find('input[type="password"]').type(senha);
  cy.contains('button', 'Entrar').click();
  cy.url().should('eq', Cypress.config().baseUrl + '/');
};

const loginAdmin = () => {
  cy.visit('/login');
  cy.get('input[type="email"]').type('admin@saboaria.com');
  cy.get('input[type="password"]').type('admin');
  cy.contains('button', 'Entrar').click();
  cy.url().should('include', '/admin');
};

// ─────────────────────────────────────────────────────────────────────────────

describe('Loop de Compras: 500 iterações por produto (ciclo)', () => {
  beforeEach(() => {
    cy.on('window:alert', () => {});
    cy.on('window:confirm', () => true);
    cy.on('uncaught:exception', () => false);
  });

  // ── 01. CADASTRO ──────────────────────────────────────────────────────────
  it('01 — cadastra cliente com endereço (SP) e cartão (Visa)', () => {
    cy.visit('/cadastro');
    cy.wait(800);

    campo('Nome Completo').type(nomeCliente, { force: true });
    campo('E-mail').type(email, { force: true });
    campo('Senha').type(senha, { force: true });
    campo('CPF').type(cpf, { force: true });
    campo('Data de Nascimento').type('1995-07-10', { force: true });
    sel('Gênero').select('Masculino', { force: true });
    campo('DDD').type('11', { force: true });
    campo('Número').type('991234567', { force: true });
    sel('Tipo de Telefone').select('Comercial', { force: true });
    cy.contains('button', 'Próximo: Endereços').click();

    campo('CEP').type('01310100', { force: true });
    cy.wait(3000);
    sel('Tipo de Logradouro').select('avenida', { force: true });
    campo('Logradouro').clear().type('Paulista', { force: true });
    campo('Número').clear().type('1500', { force: true });
    campo('Bairro').clear().type('Bela Vista', { force: true });
    campo('Cidade').clear().type('São Paulo', { force: true });
    campo('Estado').clear().type('SP', { force: true });
    campo('País').clear().type('Brasil', { force: true });
    campo('Complemento').type('Apto 10', { force: true });
    sel('Tipo de Residência').select('Apartamento', { force: true });
    campo('Apelido').type('SP Principal', { force: true });
    cy.contains('button', 'Próximo: Cartões').click();

    campo('Número do Cartão').type('4111111111111111', { force: true });
    campo('Nome Impresso').type('LOOP COMPRAS', { force: true });
    sel('Bandeira').select('visa', { force: true });
    campo('CVV').type('123', { force: true });
    cy.contains('label', labelRegex('Validade'))
      .parent().find('input[type="month"]')
      .invoke('val', '2028-12').trigger('input').trigger('change');
    campo('Apelido do Cartão').type(aliasCartao, { force: true });

    cy.contains('button', 'Finalizar Cadastro').click();
    cy.url().should('include', '/login');
  });

  // ── 02. LOOP 500 COMPRAS ──────────────────────────────────────────────────
  it('02 — executa 500 compras (ciclo de produtos) + admin aprova/despacha/entrega', () => {
    for (let i = 0; i < 500; i++) {
      const produto = PRODUTOS[i % PRODUTOS.length];
      cy.log(`━━━ ${i + 1}/500 — ${produto} ━━━`);

      // ── Cliente: compra ────────────────────────────────────────────────
      loginCliente();

      cy.intercept('GET', '**/estoques').as('est');
      cy.visit('/produtos');
      cy.wait('@est');

      cy.contains('.bg-white.border.border-stone-100.rounded-2xl', produto)
        .find('button').click({ force: true });
      cy.wait(500);

      cy.get('header').find('a[href="/carrinho"]').click();
      cy.contains('a', 'Finalizar Compra').click();
      cy.wait(1500);

      cy.contains('button', /Confirmar e Pagar/).invoke('text').then((text) => {
        const total = parseFloat((text.split('R$')[1] ?? '0').trim());
        cy.contains('p', aliasCartao)
          .closest('.overflow-hidden')
          .find('input[type="number"]')
          .invoke('val', total.toFixed(2))
          .trigger('input');
      });

      cy.contains('button', /Confirmar e Pagar/).should('not.be.disabled').click();
      cy.url().should('include', '/pedidos');
      cy.contains('button', 'Sair').click();
      cy.url().should('include', '/login');

      // ── Admin: aprovar → despachar → entregar ─────────────────────────
      loginAdmin();
      cy.contains('a', 'Vendas').click();
      cy.url().should('include', '/admin/vendas');
      cy.wait(800);

      cy.contains('button', 'Em Processamento').click();
      cy.wait(500);
      cy.get('tr').contains('button', 'Aprovar').first().click();

      cy.contains('button', 'Aprovada').click();
      cy.wait(500);
      cy.get('tr').contains('button', 'Despachar').first().click();

      cy.contains('button', 'Em Transporte').click();
      cy.wait(500);
      cy.get('tr').contains('button', 'Confirmar Entrega').first().click();

      cy.contains('button', 'Sair do Painel').click();
      cy.url().should('include', '/login');
    }
  });
});
