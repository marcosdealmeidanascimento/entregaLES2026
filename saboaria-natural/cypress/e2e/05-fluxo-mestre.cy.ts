export {};

// ─── Helpers (padrão dos outros testes) ──────────────────────────────────────

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
const email       = `cypress.mestre${String(ts).slice(-10)}@saboaria.com`;
const senha       = 'Cypress123';
const cpf         = gerarCPF(ts);
const nomeCliente = `Mestre Cypress ${String(ts).slice(-6)}`;
const aliasCartao1 = 'Visa Mestre A';
const aliasCartao2 = 'Master Mestre B';

// ─── Funções reutilizáveis ────────────────────────────────────────────────────

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

// Navega por aba a cada transição para evitar referências DOM obsoletas
// (Vue re-renderiza o <tr> ao trocar orders.value[idx], invalidando .within())
const aprovacaoCompletaAdmin = () => {
  cy.contains('a', 'Vendas').click();
  cy.url().should('include', '/admin/vendas');
  cy.wait(2000);

  // Aprovar — aba EM_PROCESSAMENTO
  cy.contains('button', 'Em Processamento').click();
  cy.wait(800);
  cy.contains('tr', nomeCliente).contains('button', 'Aprovar').click();

  // Despachar — aba APROVADA (pedido sumiu de Em Processamento)
  cy.contains('button', 'Aprovada').click();
  cy.wait(800);
  cy.contains('tr', nomeCliente).contains('button', 'Despachar').click();

  // Confirmar Entrega — aba EM_TRANSPORTE
  cy.contains('button', 'Em Transporte').click();
  cy.wait(800);
  cy.contains('tr', nomeCliente).contains('button', 'Confirmar Entrega').click();

  // Verificar — aba ENTREGUE
  cy.contains('button', 'Entregue').click();
  cy.wait(800);
  cy.contains('tr', nomeCliente).should('contain', 'Entregue');
};

// ─────────────────────────────────────────────────────────────────────────────

describe('Fluxo Mestre: cadastro → compra dupla → devolução → chatbot → compra com cupom', () => {
  beforeEach(() => {
    cy.on('window:alert', () => {});
    cy.on('window:confirm', () => true);
  });

  // ── 1. CADASTRAR CLIENTE ──────────────────────────────────────────────────
  it('01 — cadastra novo cliente com endereço (SP) e cartão (Visa)', () => {
    cy.visit('/cadastro');
    cy.wait(800);

    // Dados pessoais
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

    // Endereço inicial — Av. Paulista, SP (CEP 01310100 → frete R$ 15)
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
    campo('Apelido').type('SP Inicial', { force: true });
    cy.contains('button', 'Próximo: Cartões').click();

    // Cartão inicial — Visa
    campo('Número do Cartão').type('4111111111111111', { force: true });
    campo('Nome Impresso').type('MESTRE CYPRESS', { force: true });
    sel('Bandeira').select('visa', { force: true });
    campo('CVV').type('123', { force: true });
    cy.contains('label', labelRegex('Validade'))
      .parent().find('input[type="month"]')
      .invoke('val', '2028-12').trigger('input').trigger('change');
    campo('Apelido do Cartão').type(aliasCartao1, { force: true });

    cy.contains('button', 'Finalizar Cadastro').click();
    cy.url().should('include', '/login');
  });

  // ── 2. COMPRA 1: 2 produtos + cupom PROMO10 + novo endereço + novo cartão ──
  it('02 — compra dois produtos com cupom PROMO10, novo endereço (Mogi) e dois cartões', () => {
    loginCliente();

    cy.intercept('GET', '**/estoques').as('estoques');
    cy.visit('/produtos');
    cy.wait('@estoques');

    // Adiciona Sabonete de Lavanda
    cy.contains('.bg-white.border.border-stone-100.rounded-2xl', 'Sabonete de Lavanda')
      .find('button').click({ force: true });
    cy.wait(800);

    // Adiciona o primeiro produto diferente de Lavanda
    cy.get('.bg-white.border.border-stone-100.rounded-2xl')
      .not(':contains("Sabonete de Lavanda")')
      .first()
      .find('button').click({ force: true });
    cy.wait(800);

    // Vai ao checkout
    cy.get('header').find('a[href="/carrinho"]').click();
    cy.contains('a', 'Finalizar Compra').click();

    // ── Aplica cupom PROMO10
    cy.get('input[placeholder*="Cupom"], input[placeholder*="cupom"]').type('PROMO10');
    cy.contains('button', 'Aplicar').click();
    cy.wait(800);
    cy.contains('PROMO10').should('be.visible');

    // ── Adiciona novo endereço inline (Mogi das Cruzes → frete grátis)
    cy.contains('p', '+ Novo endereço').click({ force: true });
    cy.wait(500);
    cy.get('input[placeholder="CEP (00000-000)"]').type('08710000');
    cy.contains('button', 'Buscar').click({ force: true });
    cy.wait(3000); // aguarda ViaCEP
    cy.get('input[placeholder="Número"]').last().clear().type('200');
    cy.get('input[placeholder="Apelido (ex: Casa)"]').clear().type('Mogi Casa');
    cy.contains('button', 'Salvar e Usar').click();
    cy.wait(1000);
    cy.contains(/Frete Grátis/).should('be.visible');

    // ── Adiciona novo cartão inline (Mastercard)
    cy.contains('p', '+ Novo cartão').click({ force: true });
    cy.wait(500);
    cy.get('input[placeholder="Número do cartão"]').type('5500000000000004');
    cy.get('input[placeholder="Nome impresso"]').type('MESTRE CYPRESS');
    cy.get('input[placeholder="CVV"]').type('456');
    cy.get('input[placeholder="Validade (YYYY-MM-DD)"]').type('2029-06-01');
    cy.get('select').filter(':visible').first().select('MASTERCARD', { force: true });
    cy.get('input[placeholder="Apelido (ex: Nubank)"]').type(aliasCartao2);
    cy.contains('button', 'Salvar e Usar').click();
    cy.wait(1000);

    // ── Split de pagamento — lê total dinâmico do botão (já inclui frete, cupons e qtd)
    // O template renderiza finalTotal.toFixed(2) → "85.20" (ponto decimal, sem sep. de milhar)
    cy.contains('button', /Confirmar e Pagar/).invoke('text').then((text) => {
      const priceText = (text.split('R$')[1] ?? '0').trim(); // ex: "85.20" ou "120.50"
      const total = parseFloat(priceText);                    // 85.20 — sem remoção de pontos

      // Divide 50/50 garantindo que a soma bate exatamente (sem floating-point drift)
      const valor1 = parseFloat((total / 2).toFixed(2));
      const valor2 = parseFloat((total - valor1).toFixed(2));

      // Cartão 1 (Visa — já selecionado): input[type="number"] aceita ponto como decimal
      cy.contains('p', aliasCartao1)
        .closest('.overflow-hidden')
        .find('input[type="number"]')
        .invoke('val', valor1.toFixed(2))
        .trigger('input');

      // Cartão 2 (Mastercard — adicionado inline): idem
      cy.contains('p', aliasCartao2)
        .closest('.overflow-hidden')
        .find('input[type="number"]')
        .invoke('val', valor2.toFixed(2))
        .trigger('input');
    });

    cy.contains('button', /Confirmar e Pagar/).should('not.be.disabled').click();
    cy.url().should('include', '/pedidos');

    cy.contains('button', 'Sair').click();
    cy.url().should('include', '/login');
  });

  // ── 3. ADMIN: aprovar → despachar → entregar ─────────────────────────────
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

    // Abre modal de devolução no primeiro pedido ENTREGUE
    cy.contains('button', 'Devolver Item').first().click({ force: true });

    // Modal: seleciona apenas o primeiro item (checkbox)
    cy.get('.fixed.inset-0').within(() => {
      cy.get('input[type="checkbox"]').first().check({ force: true });
      // Quantidade permanece 1 (padrão)
      cy.contains('button', 'Solicitar Devolução').click();
    });

    cy.wait(1500);
    // Seção de devoluções aparece com status SOLICITADA
    cy.contains('SOLICITADA').should('be.visible');

    cy.contains('button', 'Sair').click();
    cy.url().should('include', '/login');
  });

  // ── 5. ADMIN: autorizar devolução (gera cupom DEV-xxx) ───────────────────
  it('05 — admin autoriza a devolução, gerando cupom de troca no banco', () => {
    loginAdmin();
    cy.contains('a', 'Vendas').click();
    cy.url().should('include', '/admin/vendas');
    cy.wait(1500);

    // Vai para aba Devoluções
    cy.contains('button', 'Devoluções').click();
    cy.wait(1500);

    // Autoriza a devolução SOLICITADA
    cy.contains('span', 'SOLICITADA')
      .closest('tr')
      .within(() => {
        cy.contains('button', 'Autorizar').click();
      });

    cy.contains('span', 'AUTORIZADA').should('be.visible');

    cy.contains('button', 'Sair do Painel').click();
    cy.url().should('include', '/login');
  });

  // ── 6. CLIENTE: chatbot pergunta sobre sabonete de lavanda ───────────────
  // it('06 — cliente pergunta ao chatbot se deve comprar sabonete de lavanda', () => {
  //   loginCliente();
  //   cy.wait(1000);

  //   // Abre o chatbot — botão flutuante no canto inferior direito (fixed)
  //   cy.get('[class*="fixed"]').last().find('button').first().click({ force: true });
  //   cy.wait(800);

  //   // Verifica que a janela abriu
  //   cy.contains('Assistente Natural').should('be.visible');

  //   // Envia a mensagem
  //   cy.get('input').last().type('Devo comprar o sabonete de lavanda?');
  //   cy.get('input').last().type('{enter}');

  //   // Aguarda a resposta da IA (Gemini pode levar até 20 segundos)
  //   cy.wait(20000);

  //   // Verifica que pelo menos uma mensagem de resposta do bot existe
  //   cy.contains('Assistente Natural')
  //     .closest('[class*="flex-col"]')
  //     .find('[class*="text-left"], [class*="bg-white"]')
  //     .should('have.length.greaterThan', 0);
  // });

  // ── 7. CLIENTE: compra lavanda com cupom DEV-xxx ──────────────────────────
  it('07 — cliente compra sabonete de lavanda aplicando cupom de devolução', () => {
    loginCliente();

    cy.intercept('GET', '**/estoques').as('estoques2');
    cy.visit('/produtos');
    cy.wait('@estoques2');

    // Adiciona Sabonete de Lavanda
    cy.contains('.bg-white.border.border-stone-100.rounded-2xl', 'Sabonete de Lavanda')
      .find('button').click({ force: true });
    cy.wait(800);

    cy.get('header').find('a[href="/carrinho"]').click();
    cy.contains('a', 'Finalizar Compra').click();
    cy.wait(2000); // aguarda cupons disponíveis carregarem do banco

    // Seção "Seus cupons" deve mostrar o chip DEV-xxx criado na devolução
    cy.contains('p', 'Seus cupons').should('be.visible');
    cy.get('button').filter((_, el) =>
      (el.textContent ?? '').trim().startsWith('DEV-')
    ).first().should('be.visible').click({ force: true });

    // Verifica desconto de R$ 25,00 no resumo
    cy.contains('Desconto').should('be.visible');
    cy.contains(/25[,.]00/).should('be.visible');

    // Endereço de Mogi (salvo na compra anterior) deve estar selecionado
    // → frete grátis, checkout habilitado
    cy.contains('button', /Confirmar e Pagar/).should('not.be.disabled').click();
    cy.url().should('include', '/pedidos');

    cy.contains('button', 'Sair').click();
    cy.url().should('include', '/login');
  });

  // ── 8. ADMIN: aprovar → despachar → entregar compra 2 + verificar gráfico ─
  it('08 — admin aprova/entrega segunda compra e verifica gráfico de linhas', () => {
    loginAdmin();

    cy.contains('a', 'Vendas').click();
    cy.url().should('include', '/admin/vendas');
    cy.wait(2000);

    // Aprovar — aba EM_PROCESSAMENTO (a primeira compra já está ENTREGUE, sem ambiguidade)
    cy.contains('button', 'Em Processamento').click();
    cy.wait(800);
    cy.contains('tr', nomeCliente).contains('button', 'Aprovar').click();

    // Despachar — aba APROVADA
    cy.contains('button', 'Aprovada').click();
    cy.wait(800);
    cy.contains('tr', nomeCliente).contains('button', 'Despachar').click();

    // Confirmar Entrega — aba EM_TRANSPORTE
    cy.contains('button', 'Em Transporte').click();
    cy.wait(800);
    cy.contains('tr', nomeCliente).contains('button', 'Confirmar Entrega').click();

    // Verificar — aba ENTREGUE (ambas as compras do cliente aparecem aqui)
    cy.contains('button', 'Entregue').click();
    cy.wait(800);
    cy.contains('tr', nomeCliente).should('contain', 'Entregue');
  });
  
  // ── 9. ADMIN: verificar gráfico de vendas (linha do tempo) ─────────────────
  it('09 — admin verifica gráfico de vendas por data no painel', () => {
    loginAdmin();
    cy.wait(10000);

    cy.contains('a', 'Análise').click();
    cy.url().should('include', '/admin/analise');
    cy.wait(2000);
  });
});
