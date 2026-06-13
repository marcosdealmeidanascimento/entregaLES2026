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

const ts = Date.now();
const email = `cypress.fluxo${String(ts).slice(-10)}@saboaria.com`;
const senha = 'Cypress123';
const cpf = gerarCPF(ts);
const nomeCliente = `Fluxo Cypress ${String(ts).slice(-6)}`;

describe('Fluxo completo: compra → entrega → troca', () => {
  beforeEach(() => {
    cy.on('window:alert', () => {});
    cy.on('window:confirm', () => true);
  });

  // ── 1. CADASTRO ──────────────────────────────────────────────────────────
  it('cadastra novo cliente com um cartão', () => {
    cy.visit('/cadastro');
    cy.wait(800);

    // Aba 1 — Dados Pessoais
    campo('Nome Completo').type(nomeCliente, { force: true });
    campo('E-mail').type(email, { force: true });
    campo('Senha').type(senha, { force: true });
    campo('CPF').type(cpf, { force: true });
    campo('Data de Nascimento').type('1992-03-15', { force: true });
    sel('Gênero').select('Feminino', { force: true });
    campo('DDD').type('11', { force: true });
    campo('Número').type('987654321', { force: true });
    sel('Tipo de Telefone').select('Comercial', { force: true });
    cy.contains('button', 'Próximo: Endereços').click();

    // Aba 2 — Endereço (CEP não-Mogi → frete R$ 15)
    campo('CEP').type('01310100', { force: true });
    cy.wait(3000);
    sel('Tipo de Logradouro').select('avenida', { force: true });
    campo('Logradouro').clear().type('Paulista', { force: true });
    campo('Número').clear().type('2000', { force: true });
    campo('Bairro').clear().type('Bela Vista', { force: true });
    campo('Cidade').clear().type('São Paulo', { force: true });
    campo('Estado').clear().type('SP', { force: true });
    campo('País').clear().type('Brasil', { force: true });
    campo('Complemento').type('Apto 42', { force: true });
    sel('Tipo de Residência').select('Apartamento', { force: true });
    campo('Observações').type('Portaria 24h', { force: true });
    campo('Apelido').type('SP Principal', { force: true });
    cy.contains('button', 'Próximo: Cartões').click();

    // Aba 3 — Cartão
    campo('Número do Cartão').type('4111111111111111', { force: true });
    campo('Nome Impresso').type('FLUXO CYPRESS', { force: true });
    sel('Bandeira').select('visa', { force: true });
    campo('CVV').type('321', { force: true });
    cy.contains('label', labelRegex('Validade'))
      .parent()
      .find('input[type="month"]')
      .invoke('val', '2029-06')
      .trigger('input')
      .trigger('change');
    campo('Apelido do Cartão').type('Visa Principal', { force: true });

    cy.contains('button', 'Finalizar Cadastro').click();
    cy.url().should('include', '/login');
  });

  // ── 2. COMPRA ─────────────────────────────────────────────────────────────
  it('cliente realiza compra do Sabonete de Lavanda', () => {
    cy.visit('/login');
    cy.get('form').find('input').first().type(email);
    cy.get('form').find('input[type="password"]').type(senha);
    cy.contains('button', 'Entrar').click();
    cy.url().should('eq', Cypress.config().baseUrl + '/');

    // Verifica que o cupom PROMO10 está disponível no perfil
    cy.visit('/perfil');
    cy.contains('button', 'Cupons').click();
    cy.contains('span', 'PROMO10').should('be.visible');

    // Adiciona produto ao carrinho
    cy.intercept('GET', '**/estoques').as('carregarEstoques');
    cy.visit('/produtos');
    cy.wait('@carregarEstoques');
    cy.contains('.bg-white.border.rounded-2xl', 'Sabonete de Lavanda')
      .find('button')
      .click({ force: true });
    cy.wait(1000);

    // Navegar via Vue Router para preservar o carrinho em memória
    cy.get('header').find('a[href="/carrinho"]').click();
    cy.contains('a', 'Finalizar Compra').click();

    // Com 1 cartão e endereço pré-selecionado o total é preenchido
    // automaticamente pelo watcher — botão já habilitado ao montar
    cy.contains('button', /Confirmar e Pagar/).should('not.be.disabled').click();
    cy.url().should('include', '/pedidos');

    cy.get('.font-mono.text-sm.font-bold.text-grafite')
      .first()
      .invoke('text')
      .then((text) => cy.log(`Pedido criado: ${text}`));
  });

  // ── 3. ADMIN: aprovar → despachar → entregar ──────────────────────────────
  it('admin: aprovar, despachar e confirmar entrega do pedido', () => {
    cy.visit('/login');
    cy.get('input[type="email"]').type('admin@saboaria.com');
    cy.get('input[type="password"]').type('admin');
    cy.contains('button', 'Entrar').click();
    cy.url().should('include', '/admin');

    // Navegar pelo sidebar (Vue Router) — preserva sessão admin em memória
    cy.contains('a', 'Vendas').click();
    cy.url().should('include', '/admin/vendas');
    cy.wait(2000);

    // Aprovar
    cy.contains('tr', nomeCliente).within(() => {
      cy.contains('span', 'Em Processamento').should('be.visible');
      cy.contains('button', 'Aprovar').click();
    });

    // Despachar — aguarda atualização reativa da linha
    cy.contains('tr', nomeCliente).within(() => {
      cy.contains('span', 'Aprovada').should('be.visible');
      cy.contains('button', 'Despachar').click();
    });

    // Confirmar Entrega
    cy.contains('tr', nomeCliente).within(() => {
      cy.contains('span', 'Em Transporte').should('be.visible');
      cy.contains('button', 'Confirmar Entrega').click();
    });

    cy.contains('tr', nomeCliente).within(() => {
      cy.contains('span', 'Entregue').should('be.visible');
    });

    cy.contains('button', 'Sair do Painel').click();
    cy.url().should('include', '/login');
  });

  // ── 4. CLIENTE: solicitar troca ───────────────────────────────────────────
  it('cliente solicita troca do pedido entregue', () => {
    cy.visit('/login');
    cy.get('form').find('input').first().type(email);
    cy.get('form').find('input[type="password"]').type(senha);
    cy.contains('button', 'Entrar').click();
    cy.url().should('eq', Cypress.config().baseUrl + '/');

    // Navegar via header (Vue Router)
    cy.contains('a', 'Meus Pedidos').click();
    cy.url().should('include', '/pedidos');
    cy.wait(2000);

    cy.contains('button', 'Solicitar Troca').should('be.visible').click();

    // Após a requisição o badge do pedido atualiza para EM_TROCA
    cy.contains('span', 'EM_TROCA').should('be.visible');
    cy.contains('button', 'Solicitar Troca').should('not.exist');

    cy.contains('button', 'Sair').click();
    cy.url().should('include', '/login');
  });

  // ── 5. ADMIN: autorizar troca → receber itens ─────────────────────────────
  it('admin: autorizar troca e confirmar recebimento dos itens', () => {
    cy.visit('/login');
    cy.get('input[type="email"]').type('admin@saboaria.com');
    cy.get('input[type="password"]').type('admin');
    cy.contains('button', 'Entrar').click();
    cy.url().should('include', '/admin');

    cy.contains('a', 'Vendas').click();
    cy.url().should('include', '/admin/vendas');
    cy.wait(2000);

    // Autorizar Troca
    cy.contains('tr', nomeCliente).within(() => {
      cy.contains('span', 'Em Troca').should('be.visible');
      cy.contains('button', 'Autorizar Troca').click();
    });

    // Receber Itens — confirm() retorna true pelo beforeEach
    cy.contains('tr', nomeCliente).within(() => {
      cy.contains('span', 'Troca Autorizada').should('be.visible');
      cy.contains('button', 'Receber Itens').click();
    });

    cy.contains('tr', nomeCliente).within(() => {
      cy.contains('span', 'Trocado').should('be.visible');
    });
  });

  // ── 6. CLIENTE: cupons no perfil ──────────────────────────────────────────
  it('cliente visualiza lista de cupons no perfil', () => {
    cy.visit('/login');
    cy.get('form').find('input').first().type(email);
    cy.get('form').find('input[type="password"]').type(senha);
    cy.contains('button', 'Entrar').click();
    cy.url().should('eq', Cypress.config().baseUrl + '/');

    cy.visit('/perfil');
    cy.contains('button', 'Cupons').click();
    cy.contains('h2', 'Meus Cupons').should('be.visible');
    cy.get('.border-dashed').should('have.length.greaterThan', 0);
  });
});
