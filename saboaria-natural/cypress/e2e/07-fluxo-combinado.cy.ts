export {};

// ─── Helpers ──────────────────────────────────────────────────────────────────

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

// ─── Dados únicos por execução ────────────────────────────────────────────────

const ts = Date.now();
const email        = `cypress.comb${String(ts).slice(-10)}@saboaria.com`;
const senha        = 'Cypress123';
const cpf          = gerarCPF(ts);
const nomeCliente  = `Comb Cypress ${String(ts).slice(-6)}`;
const aliasCartao1 = 'Visa Comb A';
const aliasCartao2 = 'Master Comb B';

const D = 80; // delay entre teclas (ms) — ajusta aqui para mais lento/rápido

// ─── Funções reutilizáveis ────────────────────────────────────────────────────

const loginCliente = () => {
  cy.visit('/login');
  cy.get('form').find('input').first().type(email, { delay: D });
  cy.get('form').find('input[type="password"]').type(senha, { delay: D });
  cy.contains('button', 'Entrar').click();
  cy.url().should('eq', Cypress.config().baseUrl + '/');
};

const loginAdmin = () => {
  cy.visit('/login');
  cy.get('input[type="email"]').type('admin@saboaria.com', { delay: D });
  cy.get('input[type="password"]').type('admin', { delay: D });
  cy.contains('button', 'Entrar').click();
  cy.url().should('include', '/admin');
};

// Navega por aba a cada transição para evitar referências DOM obsoletas
const aprovacaoCompletaAdmin = () => {
  cy.contains('a', 'Vendas').click();
  cy.url().should('include', '/admin/vendas');
  cy.wait(2000);

  cy.contains('button', 'Em Processamento').click();
  cy.wait(1500);
  cy.contains('tr', nomeCliente).contains('button', 'Aprovar').click();

  cy.contains('button', 'Aprovada').click();
  cy.wait(1500);
  cy.contains('tr', nomeCliente).contains('button', 'Despachar').click();

  cy.contains('button', 'Em Transporte').click();
  cy.wait(1500);
  cy.contains('tr', nomeCliente).contains('button', 'Confirmar Entrega').click();

  cy.contains('button', 'Entregue').click();
  cy.wait(1500);
  cy.contains('tr', nomeCliente).should('contain', 'Entregue');
};

// ─────────────────────────────────────────────────────────────────────────────

describe('Fluxo Combinado: cadastro → compra dupla → devolução → 2 cupons → entrega', () => {
  beforeEach(() => {
    cy.on('window:alert', () => {});
    cy.on('window:confirm', () => true);
  });

  // ── 1. CADASTRAR CLIENTE ──────────────────────────────────────────────────
  it('01 — cadastra novo cliente com endereço (SP) e cartão Visa', () => {
    cy.visit('/cadastro');
    cy.wait(1500);

    campo('Nome Completo').type(nomeCliente, { force: true, delay: D });
    campo('E-mail').type(email, { force: true, delay: D });
    campo('Senha').type(senha, { force: true, delay: D });
    campo('CPF').type(cpf, { force: true, delay: D });
    campo('Data de Nascimento').type('1995-07-10', { force: true, delay: D });
    sel('Gênero').select('Masculino', { force: true });
    campo('DDD').type('11', { force: true, delay: D });
    campo('Número').type('991234567', { force: true, delay: D });
    sel('Tipo de Telefone').select('Comercial', { force: true });
    cy.contains('button', 'Próximo: Endereços').click();
    cy.wait(1000);

    campo('CEP').type('01310100', { force: true, delay: D });
    cy.wait(3000);
    sel('Tipo de Logradouro').select('avenida', { force: true });
    campo('Logradouro').clear().type('Paulista', { force: true, delay: D });
    campo('Número').clear().type('1500', { force: true, delay: D });
    campo('Bairro').clear().type('Bela Vista', { force: true, delay: D });
    campo('Cidade').clear().type('São Paulo', { force: true, delay: D });
    campo('Estado').clear().type('SP', { force: true, delay: D });
    campo('País').clear().type('Brasil', { force: true, delay: D });
    campo('Complemento').type('Apto 10', { force: true, delay: D });
    sel('Tipo de Residência').select('Apartamento', { force: true });
    campo('Apelido').type('SP Inicial', { force: true, delay: D });
    cy.contains('button', 'Próximo: Cartões').click();
    cy.wait(1000);

    campo('Número do Cartão').type('4111111111111111', { force: true, delay: D });
    campo('Nome Impresso').type('COMB CYPRESS', { force: true, delay: D });
    sel('Bandeira').select('visa', { force: true });
    campo('CVV').type('123', { force: true, delay: D });
    cy.contains('label', labelRegex('Validade'))
      .parent().find('input[type="month"]')
      .invoke('val', '2028-12').trigger('input').trigger('change');
    campo('Apelido do Cartão').type(aliasCartao1, { force: true, delay: D });

    cy.contains('button', 'Finalizar Cadastro').click();
    cy.url().should('include', '/login');
  });

  // ── 2. COMPRA 1: 2 produtos, sem cupom, novo endereço + novo cartão no checkout ──
  it('02 — compra dois produtos sem cupom, com dois cartões (split 50/50)', () => {
    loginCliente();

    cy.intercept('GET', '**/estoques').as('estoques');
    cy.visit('/produtos');
    cy.wait('@estoques');
    cy.wait(1200);

    cy.contains('.bg-white.border.border-stone-100.rounded-2xl', 'Sabonete de Lavanda')
      .find('button').click({ force: true });
    cy.wait(1500);

    cy.get('.bg-white.border.border-stone-100.rounded-2xl')
      .not(':contains("Sabonete de Lavanda")')
      .first()
      .find('button').click({ force: true });
    cy.wait(1500);

    cy.get('header').find('a[href="/carrinho"]').click();
    cy.wait(1000);
    cy.contains('a', 'Finalizar Compra').click();
    cy.wait(1000);

    // Adiciona novo endereço inline (Mogi das Cruzes → frete grátis)
    cy.contains('p', '+ Novo endereço').click({ force: true });
    cy.wait(1000);
    cy.get('input[placeholder="CEP (00000-000)"]').type('08710000', { delay: D });
    cy.contains('button', 'Buscar').click({ force: true });
    cy.wait(3000);
    cy.get('input[placeholder="Número"]').last().clear().type('200', { delay: D });
    cy.get('input[placeholder="Apelido (ex: Casa)"]').clear().type('Mogi Casa', { delay: D });
    cy.contains('button', 'Salvar e Usar').click();
    cy.wait(1500);
    cy.contains(/Frete Grátis/).should('be.visible');

    // Adiciona novo cartão inline (Mastercard)
    cy.contains('p', '+ Novo cartão').click({ force: true });
    cy.wait(1000);
    cy.get('input[placeholder="Número do cartão"]').type('5500000000000004', { delay: D });
    cy.get('input[placeholder="Nome impresso"]').type('COMB CYPRESS', { delay: D });
    cy.get('input[placeholder="CVV"]').type('456', { delay: D });
    cy.get('input[placeholder="Validade (YYYY-MM-DD)"]').type('2029-06-01', { delay: D });
    cy.get('select').filter(':visible').first().select('MASTERCARD', { force: true });
    cy.get('input[placeholder="Apelido (ex: Nubank)"]').type(aliasCartao2, { delay: D });
    cy.contains('button', 'Salvar e Usar').click();
    cy.wait(1500);

    // Split 50/50 entre os dois cartões
    cy.contains('button', /Confirmar e Pagar/).invoke('text').then((text) => {
      const priceText = (text.split('R$')[1] ?? '0').trim();
      const total = parseFloat(priceText);
      const valor1 = parseFloat((total / 2).toFixed(2));
      const valor2 = parseFloat((total - valor1).toFixed(2));

      cy.contains('p', aliasCartao1)
        .closest('.overflow-hidden')
        .find('input[type="number"]')
        .invoke('val', valor1.toFixed(2))
        .trigger('input');

      cy.contains('p', aliasCartao2)
        .closest('.overflow-hidden')
        .find('input[type="number"]')
        .invoke('val', valor2.toFixed(2))
        .trigger('input');
    });

    cy.wait(1500);
    cy.contains('button', /Confirmar e Pagar/).should('not.be.disabled').click();
    cy.url().should('include', '/pedidos');

    cy.contains('button', 'Sair').click();
    cy.url().should('include', '/login');
  });

  // ── 3. ADMIN: aprovar → despachar → entregar compra 1 ────────────────────
  it('03 — admin aprova, despacha e confirma entrega da compra 1', () => {
    loginAdmin();
    aprovacaoCompletaAdmin();
    cy.contains('button', 'Sair do Painel').click();
    cy.url().should('include', '/login');
  });

  // ── 4. CLIENTE: solicitar devolução de 1 item ─────────────────────────────
  it('04 — cliente solicita devolução de um item do pedido entregue', () => {
    loginCliente();
    cy.contains('a', 'Meus Pedidos').click();
    cy.url().should('include', '/pedidos');
    cy.wait(2500);

    cy.contains('button', 'Devolver Item').first().click({ force: true });
    cy.wait(1000);

    cy.get('.fixed.inset-0').within(() => {
      cy.get('input[type="checkbox"]').first().check({ force: true });
      cy.wait(800);
      cy.contains('button', 'Solicitar Devolução').click();
    });

    cy.wait(2000);
    cy.contains('SOLICITADA').should('be.visible');

    cy.contains('button', 'Sair').click();
    cy.url().should('include', '/login');
  });

  // ── 5. ADMIN: autorizar devolução (gera cupom DEV-xxx) ───────────────────
  it('05 — admin autoriza a devolução, gerando cupom de troca', () => {
    loginAdmin();
    cy.contains('a', 'Vendas').click();
    cy.url().should('include', '/admin/vendas');
    cy.wait(1500);

    cy.contains('button', 'Devoluções').click();
    cy.wait(2000);

    cy.contains('span', 'SOLICITADA')
      .closest('tr')
      .within(() => {
        cy.contains('button', 'Autorizar').click();
      });

    cy.wait(1000);
    cy.contains('span', 'AUTORIZADA').should('be.visible');

    cy.contains('button', 'Sair do Painel').click();
    cy.url().should('include', '/login');
  });

  // ── 6. CLIENTE: compra 1 produto com cupom DEV-xxx + PROMO10 ─────────────
  it('06 — cliente compra produto aplicando cupom DEV-xxx e PROMO10', () => {
    loginCliente();

    cy.intercept('GET', '**/estoques').as('estoques2');
    cy.visit('/produtos');
    cy.wait('@estoques2');
    cy.wait(1200);

    cy.contains('.bg-white.border.border-stone-100.rounded-2xl', 'Sabonete de Lavanda')
      .find('button').click({ force: true });
    cy.wait(1500);

    cy.get('header').find('a[href="/carrinho"]').click();
    cy.wait(1000);
    cy.contains('a', 'Finalizar Compra').click();
    cy.wait(2000);

    // Aplica cupom DEV-xxx via chip "Seus cupons"
    cy.contains('p', 'Seus cupons').should('be.visible');
    cy.get('button').filter((_, el) =>
      (el.textContent ?? '').trim().startsWith('DEV-')
    ).first().should('be.visible').click({ force: true });
    cy.wait(1500);

    // Aplica cupom PROMO10 via input
    cy.get('input[placeholder*="Cupom"], input[placeholder*="cupom"]').type('PROMO10', { delay: D });
    cy.contains('button', 'Aplicar').click();
    cy.wait(1500);

    cy.wait(1500);
    cy.contains('button', /Confirmar e Pagar/).should('not.be.disabled').click();
    cy.url().should('include', '/pedidos');

    cy.contains('button', 'Sair').click();
    cy.url().should('include', '/login');
  });

  // ── 7. ADMIN: aprovar → despachar → entregar compra 2 ────────────────────
  it('07 — admin aprova, despacha e confirma entrega da compra 2', () => {
    loginAdmin();
    aprovacaoCompletaAdmin();
    cy.contains('button', 'Sair do Painel').click();
    cy.url().should('include', '/login');
  });
});
