-- =============================================================
-- Seed: Cliente + Endereço + Cartão
--       4 Categorias novas (total: 10)
--       10 Produtos novos (total: 20)
--       Estoque dos 10 novos produtos
--       2 Cupons novos
--       300 Vendas — Jan/2024 a Jun/2026
--
-- Pré-requisitos (já executados):
--   readme.md    → schema base + 3 grupos + 6 categorias
--   migration.sql → tabelas extras (cup_cupom, dev_*, ven_cupom_venda,
--                   colunas de auditoria, 2 cupons: PROMO10, DESCONTO15)
--   seed_produtos.sql → 10 produtos
--   seed_estoque.sql  → 10.000 unidades p/ cada produto
--
-- ATENÇÃO: NÃO rodar seed_vendas.sql junto com este script.
--          Rodar um dos dois, não os dois — para manter 300 vendas.
--
-- Uso:
--   mysql -u gigi -pcecilia gigi < seed_cliente_vendas_2024.sql
-- =============================================================

-- -------------------------------------------------------------
-- 1. GRUPOS DE PRECIFICAÇÃO (3)
--    WHERE NOT EXISTS para ser idempotente caso seed.sql já tenha rodado.
-- -------------------------------------------------------------
INSERT INTO grp_grupo_precificacao (grp_nome, grp_piso_preco, grp_teto_preco, grp_percentual_margem)
SELECT 'Linha Básica', 8.00, 20.00, 80.00
WHERE  NOT EXISTS (SELECT 1 FROM grp_grupo_precificacao WHERE grp_nome = 'Linha Básica');

INSERT INTO grp_grupo_precificacao (grp_nome, grp_piso_preco, grp_teto_preco, grp_percentual_margem)
SELECT 'Linha Premium', 18.00, 60.00, 120.00
WHERE  NOT EXISTS (SELECT 1 FROM grp_grupo_precificacao WHERE grp_nome = 'Linha Premium');

INSERT INTO grp_grupo_precificacao (grp_nome, grp_piso_preco, grp_teto_preco, grp_percentual_margem)
SELECT 'Linha Especial', 30.00, 100.00, 150.00
WHERE  NOT EXISTS (SELECT 1 FROM grp_grupo_precificacao WHERE grp_nome = 'Linha Especial');

-- -------------------------------------------------------------
-- 2. CATEGORIAS BASE (6)
--    Mesmas do seed.sql — WHERE NOT EXISTS para idempotência.
-- -------------------------------------------------------------
INSERT INTO cat_categoria (cat_nome, cat_descricao)
SELECT 'Florais', 'Sabonetes com essências de flores como lavanda, rosa e jasmin'
WHERE  NOT EXISTS (SELECT 1 FROM cat_categoria WHERE cat_nome = 'Florais');

INSERT INTO cat_categoria (cat_nome, cat_descricao)
SELECT 'Cítricos', 'Sabonetes refrescantes com limão, laranja e tangerina'
WHERE  NOT EXISTS (SELECT 1 FROM cat_categoria WHERE cat_nome = 'Cítricos');

INSERT INTO cat_categoria (cat_nome, cat_descricao)
SELECT 'Terrosos', 'Sabonetes com argila, café e elementos da terra'
WHERE  NOT EXISTS (SELECT 1 FROM cat_categoria WHERE cat_nome = 'Terrosos');

INSERT INTO cat_categoria (cat_nome, cat_descricao)
SELECT 'Especiarias', 'Sabonetes com canela, cravo, gengibre e outras especiarias'
WHERE  NOT EXISTS (SELECT 1 FROM cat_categoria WHERE cat_nome = 'Especiarias');

INSERT INTO cat_categoria (cat_nome, cat_descricao)
SELECT 'Herbais', 'Sabonetes com ervas medicinais como camomila, hortelã e alecrim'
WHERE  NOT EXISTS (SELECT 1 FROM cat_categoria WHERE cat_nome = 'Herbais');

INSERT INTO cat_categoria (cat_nome, cat_descricao)
SELECT 'Neutros', 'Sabonetes sem fragrância, indicados para pele sensível'
WHERE  NOT EXISTS (SELECT 1 FROM cat_categoria WHERE cat_nome = 'Neutros');

-- -------------------------------------------------------------
-- 3. CLIENTE
-- -------------------------------------------------------------
INSERT INTO cli_cliente
  (cli_nome, cli_genero, cli_cpf, cli_email,
   cli_telefone, cli_tipo_telefone, cli_telefone_ddd,
   cli_data_nascimento, cli_ranking, cli_status, cli_senha)
VALUES
  ('Ana Clara Ferreira Santos', 'F', '345.678.901-23',
   'ana.clara@saboanatural.com', '987654321', 'CELULAR', '11',
   '1990-05-15', 0, 1,
   '$2a$10$xYzAbCdEfGhIjKlMnOpQrOeKvWcMTa2PqS8XlJZ5dRuN0A7B1C3eS');

SET @cli_id = LAST_INSERT_ID();

-- -------------------------------------------------------------
-- 2. ENDEREÇO (favorito + cobrança)
-- -------------------------------------------------------------
INSERT INTO end_endereco
  (end_cep, end_tipo_residencia, end_logradouro, end_tipo_logradouro,
   end_numero, end_bairro, end_cidade, end_estado, end_pais,
   end_complemento, end_apelido, end_observacao,
   end_favorito, end_cobranca, cli_id)
VALUES
  ('01310-100', 'CASA', 'Flores', 'Rua',
   '123', 'Jardim Paulista', 'São Paulo', 'SP', 'Brasil',
   'Apto 42', 'Casa', NULL,
   1, 1, @cli_id);

-- -------------------------------------------------------------
-- 3. CARTÃO (favorito)
-- -------------------------------------------------------------
INSERT INTO car_cartao
  (car_numero, car_nome_impresso, car_cvv,
   car_bandeira, car_validade, car_apelido, car_favorito, cli_id)
VALUES
  ('4111111111111111', 'ANA CLARA F SANTOS', '123',
   'VISA', '2028-12-01', 'Cartão Principal', 1, @cli_id);

SET @car_id = LAST_INSERT_ID();

-- -------------------------------------------------------------
-- 4. CATEGORIAS NOVAS (4 — soma-se às 6 existentes → total 10)
--    Usa WHERE NOT EXISTS para ser idempotente.
-- -------------------------------------------------------------
INSERT INTO cat_categoria (cat_nome, cat_descricao)
SELECT 'Óleos Essenciais',
       'Óleos naturais concentrados para aromaterapia e cuidados com a pele'
WHERE  NOT EXISTS (SELECT 1 FROM cat_categoria WHERE cat_nome = 'Óleos Essenciais');

INSERT INTO cat_categoria (cat_nome, cat_descricao)
SELECT 'Kits Presente',
       'Combinações especiais de produtos para presentear em datas comemorativas'
WHERE  NOT EXISTS (SELECT 1 FROM cat_categoria WHERE cat_nome = 'Kits Presente');

INSERT INTO cat_categoria (cat_nome, cat_descricao)
SELECT 'Sabonetes Líquidos',
       'Sabonetes em formato líquido, práticos e com diferentes fragrâncias'
WHERE  NOT EXISTS (SELECT 1 FROM cat_categoria WHERE cat_nome = 'Sabonetes Líquidos');

INSERT INTO cat_categoria (cat_nome, cat_descricao)
SELECT 'Edição Especial',
       'Produtos sazonais e edições limitadas com fórmulas exclusivas'
WHERE  NOT EXISTS (SELECT 1 FROM cat_categoria WHERE cat_nome = 'Edição Especial');

-- -------------------------------------------------------------
-- 5. PRODUTOS NOVOS (10 — soma-se aos 10 existentes → total 20)
--    Subselects em grp_id e cat_id para não depender de IDs fixos.
-- -------------------------------------------------------------
INSERT INTO pro_produto
  (pro_nome, pro_descricao, pro_peso, pro_aroma, pro_ingredientes,
   pro_indicacao, pro_foto, pro_dimensoes, pro_validade, grp_id, cat_id)
VALUES

-- Óleos Essenciais / Especial
(
  'Óleo Essencial de Lavanda',
  'Óleo puro de lavanda 100%, calmante e relaxante. Ideal para aromaterapia e massagem.',
  30, 'Lavanda',
  'Lavandula angustifolia (lavanda) óleo essencial puro 100%',
  'Difusores, massagem corporal e banho aromático',
  NULL, '3x3x8cm', '36 meses',
  (SELECT grp_id FROM grp_grupo_precificacao WHERE grp_nome = 'Linha Especial'),
  (SELECT cat_id FROM cat_categoria WHERE cat_nome = 'Óleos Essenciais')
),
(
  'Blend Relaxante Noturno',
  'Mistura aromática de camomila, ylang-ylang e sândalo para promover sono profundo.',
  30, 'Camomila e Sândalo',
  'Óleos essenciais de camomila romana, ylang-ylang, sândalo e óleo carreador de amêndoas',
  'Difusor e massagem antes de dormir',
  NULL, '3x3x8cm', '24 meses',
  (SELECT grp_id FROM grp_grupo_precificacao WHERE grp_nome = 'Linha Especial'),
  (SELECT cat_id FROM cat_categoria WHERE cat_nome = 'Óleos Essenciais')
),

-- Kits Presente / Especial
(
  'Kit Spa em Casa',
  'Kit completo com sabonete de lavanda, óleo de amêndoas e sais de banho artesanais.',
  350, 'Floral e amadeirado',
  'Sabonete de lavanda (100g), óleo de amêndoas doces (50ml), sais do Himalaia (150g), pétalas de rosas secas',
  'Presente para ocasiões especiais; uso no banho relaxante',
  NULL, '20x15x8cm', '18 meses',
  (SELECT grp_id FROM grp_grupo_precificacao WHERE grp_nome = 'Linha Especial'),
  (SELECT cat_id FROM cat_categoria WHERE cat_nome = 'Kits Presente')
),
(
  'Kit Dia das Mães',
  'Kit premium com sabonete artesanal de rosa e creme hidratante de karité.',
  280, 'Rosa e Baunilha',
  'Sabonete de rosa (120g), creme hidratante de karité (80g), mini óleo essencial de baunilha (10ml)',
  'Presente para o Dia das Mães e datas especiais',
  NULL, '18x12x6cm', '18 meses',
  (SELECT grp_id FROM grp_grupo_precificacao WHERE grp_nome = 'Linha Especial'),
  (SELECT cat_id FROM cat_categoria WHERE cat_nome = 'Kits Presente')
),

-- Sabonetes Líquidos / Básica
(
  'Sabonete Líquido de Lavanda',
  'Sabonete líquido suave com óleo essencial de lavanda. Antibacteriano natural.',
  250, 'Lavanda',
  'Água purificada, lauril glucosídeo, glicerina vegetal, óleo essencial de lavanda, extrato de calêndula',
  'Lavagem das mãos; pele normal a seca',
  NULL, '7x7x18cm', '24 meses',
  (SELECT grp_id FROM grp_grupo_precificacao WHERE grp_nome = 'Linha Básica'),
  (SELECT cat_id FROM cat_categoria WHERE cat_nome = 'Sabonetes Líquidos')
),
(
  'Sabonete Líquido Cítrico Energizante',
  'Sabonete líquido refrescante com limão siciliano e hortelã. Revigorante e purificante.',
  250, 'Limão e Hortelã',
  'Água purificada, lauril glucosídeo, óleo essencial de limão siciliano, extrato de hortelã, aloe vera',
  'Uso diário; pele oleosa e mista',
  NULL, '7x7x18cm', '24 meses',
  (SELECT grp_id FROM grp_grupo_precificacao WHERE grp_nome = 'Linha Básica'),
  (SELECT cat_id FROM cat_categoria WHERE cat_nome = 'Sabonetes Líquidos')
),

-- Edição Especial / Premium
(
  'Sabonete de Camomila e Mel (Ed. Especial)',
  'Edição limitada ultra-calmante com camomila orgânica e mel silvestre puro.',
  110, 'Camomila e Mel',
  'Óleo de coco, mel silvestre, extrato de camomila orgânica, manteiga de cacau, vitamina E',
  'Pele sensível, irritada ou com reações alérgicas',
  NULL, '8x5x3cm', '18 meses',
  (SELECT grp_id FROM grp_grupo_precificacao WHERE grp_nome = 'Linha Premium'),
  (SELECT cat_id FROM cat_categoria WHERE cat_nome = 'Edição Especial')
),
(
  'Sabonete de Alecrim e Eucalipto (Ed. Especial)',
  'Edição limitada estimulante com alecrim e eucalipto. Ideal para o início do dia.',
  100, 'Alecrim e Eucalipto',
  'Óleo de coco, óleo essencial de alecrim, óleo essencial de eucalipto, argila cinza',
  'Todos os tipos de pele; uso matinal e pós-exercício',
  NULL, '7x5x3cm', '18 meses',
  (SELECT grp_id FROM grp_grupo_precificacao WHERE grp_nome = 'Linha Premium'),
  (SELECT cat_id FROM cat_categoria WHERE cat_nome = 'Edição Especial')
),

-- Florais / Premium (reforça categoria existente)
(
  'Sabonete de Peônia',
  'Sabonete delicado com extrato natural de peônia. Floral suave e altamente hidratante.',
  100, 'Peônia',
  'Óleo de coco, extrato de peônia, manteiga de karité, óleo essencial de peônia, vitamina E',
  'Pele normal a seca; uso noturno',
  NULL, '7x5x3cm', '24 meses',
  (SELECT grp_id FROM grp_grupo_precificacao WHERE grp_nome = 'Linha Premium'),
  (SELECT cat_id FROM cat_categoria WHERE cat_nome = 'Florais')
),

-- Cítricos / Básica (reforça categoria existente)
(
  'Sabonete de Tangerina e Hortelã',
  'Sabonete refrescante e energizante com tangerina brasileira e hortelã fresca.',
  90, 'Tangerina e Hortelã',
  'Óleo de coco, óleo essencial de tangerina, extrato de hortelã, argila branca',
  'Pele oleosa e mista; uso matinal',
  NULL, '7x4x3cm', '18 meses',
  (SELECT grp_id FROM grp_grupo_precificacao WHERE grp_nome = 'Linha Básica'),
  (SELECT cat_id FROM cat_categoria WHERE cat_nome = 'Cítricos')
);

-- -------------------------------------------------------------
-- 6. ESTOQUE DOS 10 NOVOS PRODUTOS
--    valorVenda = valorCusto × (1 + percentualMargem/100)
--    Básica=80% → ×1,80 | Premium=120% → ×2,20 | Especial=150% → ×2,50
-- -------------------------------------------------------------
INSERT INTO est_estoque
  (pro_id, est_quantidade, est_valor_custo, est_valor_venda,
   est_fornecedor, est_data_entrada)
SELECT
  p.pro_id,
  5000,
  CASE g.grp_nome
    WHEN 'Linha Básica'   THEN 10.00
    WHEN 'Linha Premium'  THEN 15.00
    WHEN 'Linha Especial' THEN 20.00
    ELSE 10.00
  END,
  CASE g.grp_nome
    WHEN 'Linha Básica'   THEN 18.00   -- 10.00 × 1,80
    WHEN 'Linha Premium'  THEN 33.00   -- 15.00 × 2,20
    WHEN 'Linha Especial' THEN 50.00   -- 20.00 × 2,50
    ELSE 18.00
  END,
  'Saboaria Natural',
  '2023-12-01'
FROM pro_produto p
JOIN grp_grupo_precificacao g ON p.grp_id = g.grp_id
WHERE p.pro_nome IN (
  'Óleo Essencial de Lavanda',
  'Blend Relaxante Noturno',
  'Kit Spa em Casa',
  'Kit Dia das Mães',
  'Sabonete Líquido de Lavanda',
  'Sabonete Líquido Cítrico Energizante',
  'Sabonete de Camomila e Mel (Ed. Especial)',
  'Sabonete de Alecrim e Eucalipto (Ed. Especial)',
  'Sabonete de Peônia',
  'Sabonete de Tangerina e Hortelã'
);

-- -------------------------------------------------------------
-- 7. CUPONS NOVOS (2)
--    Os 2 existentes (PROMO10, DESCONTO15) estão em migration.sql.
-- -------------------------------------------------------------
INSERT INTO cup_cupom
  (cup_codigo, cup_valor, cup_tipo, cup_ativo, cup_eh_troca, cup_cliente_id)
SELECT 'NATAL25', 25.00, 'PERCENT', TRUE, FALSE, NULL
WHERE  NOT EXISTS (SELECT 1 FROM cup_cupom WHERE cup_codigo = 'NATAL25');

INSERT INTO cup_cupom
  (cup_codigo, cup_valor, cup_tipo, cup_ativo, cup_eh_troca, cup_cliente_id)
SELECT 'FRETEGRATIS', 20.00, 'FIXED', TRUE, FALSE, NULL
WHERE  NOT EXISTS (SELECT 1 FROM cup_cupom WHERE cup_codigo = 'FRETEGRATIS');

-- -------------------------------------------------------------
-- 8. STORED PROCEDURE: 300 VENDAS  Jan/2024 – Jun/2026
-- -------------------------------------------------------------
DROP PROCEDURE IF EXISTS gerar_vendas_2024;

DELIMITER $$

CREATE PROCEDURE gerar_vendas_2024(IN p_cli BIGINT, IN p_car BIGINT)
BEGIN
  DECLARE i           INT     DEFAULT 1;
  DECLARE v_data      DATE;
  DECLARE v_ven_id    BIGINT;
  DECLARE v_pro_id    BIGINT;
  DECLARE v_preco     DECIMAL(10,2) DEFAULT 25.00;
  DECLARE v_qtd       INT;
  DECLARE v_total     DECIMAL(10,2);
  DECLARE v_frete     DECIMAL(10,2);
  DECLARE v_num_itens INT;
  DECLARE j           INT;

  -- Absorve NOT FOUND de qualquer SELECT INTO sem encerrar a procedure.
  DECLARE CONTINUE HANDLER FOR NOT FOUND BEGIN END;

  -- Tabela temporária com os IDs reais dos 20 produtos.
  DROP TEMPORARY TABLE IF EXISTS tmp_pro;
  CREATE TEMPORARY TABLE tmp_pro (pro_id BIGINT NOT NULL);
  INSERT INTO tmp_pro SELECT pro_id FROM pro_produto ORDER BY pro_id;

  WHILE i <= 300 DO

    -- Data aleatória em 895 dias: 2024-01-01 a ~2026-06-13
    SET v_data      = DATE_ADD('2024-01-01', INTERVAL FLOOR(RAND() * 895) DAY);
    SET v_frete     = IF(RAND() > 0.55, 0.00, 5.90);
    SET v_total     = 0.00;
    SET v_num_itens = 1 + FLOOR(RAND() * 3);   -- 1, 2 ou 3 itens

    INSERT INTO ven_venda
      (cli_id, ven_data, ven_status, ven_frete, ven_valor_total)
    VALUES
      (p_cli, v_data, 'ENTREGUE', v_frete, 0.00);

    SET v_ven_id = LAST_INSERT_ID();

    SET j = 1;
    WHILE j <= v_num_itens DO

      -- Produto aleatório dentre os 20
      SELECT pro_id INTO v_pro_id
      FROM   tmp_pro
      ORDER  BY RAND()
      LIMIT  1;

      SET v_qtd   = 1 + FLOOR(RAND() * 2);   -- 1 ou 2 unidades
      SET v_preco = 25.00;                    -- fallback

      SELECT est_valor_venda INTO v_preco
      FROM   est_estoque
      WHERE  pro_id = v_pro_id
      ORDER  BY est_data_entrada DESC
      LIMIT  1;

      INSERT INTO ven_item_venda
        (ven_id, pro_id, ven_item_quantidade,
         ven_item_valor_unitario, ven_item_valor_total)
      VALUES
        (v_ven_id, v_pro_id, v_qtd,
         v_preco, ROUND(v_preco * v_qtd, 2));

      SET v_total = v_total + ROUND(v_preco * v_qtd, 2);
      SET j = j + 1;

    END WHILE;

    -- Atualiza o total da venda (itens + frete)
    UPDATE ven_venda
    SET    ven_valor_total = ROUND(v_total + v_frete, 2)
    WHERE  ven_id = v_ven_id;

    -- Pagamento integral com o cartão do cliente
    INSERT INTO ven_pagamento_venda (ven_id, car_id, pag_valor)
    VALUES (v_ven_id, p_car, ROUND(v_total + v_frete, 2));

    SET i = i + 1;

  END WHILE;

  DROP TEMPORARY TABLE IF EXISTS tmp_pro;

END$$

DELIMITER ;

CALL gerar_vendas_2024(@cli_id, @car_id);
DROP PROCEDURE IF EXISTS gerar_vendas_2024;

