-- =============================================================
-- IMPORTANTE: após rodar este schema base, execute também:
--   migration.sql  — adiciona cupons, devoluções e colunas de auditoria
-- =============================================================

--
-- Excluir tabelas existentes (opcional, para recriar o banco do zero)
-- A ordem é importante devido às chaves estrangeiras.
--
DROP TABLE IF EXISTS car_cartao;
DROP TABLE IF EXISTS end_endereco;
DROP TABLE IF EXISTS cli_cliente;

--
-- Criação da tabela de Clientes
--
CREATE TABLE cli_cliente (
    cli_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cli_nome VARCHAR(255) NOT NULL,
    cli_genero VARCHAR(50),
    cli_cpf VARCHAR(14) UNIQUE NOT NULL,
    cli_email VARCHAR(255) UNIQUE NOT NULL,
    cli_telefone VARCHAR(20),
    cli_tipo_telefone VARCHAR(50),
    cli_telefone_ddd VARCHAR(5),
    cli_data_nascimento DATE,
    cli_ranking BIGINT,
    cli_status BOOLEAN NOT NULL,
    cli_senha VARCHAR(255) NOT NULL
);

--
-- Criação da tabela de Endereços
--
CREATE TABLE end_endereco (
    end_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    end_cep VARCHAR(10) NOT NULL,
    end_tipo_residencia VARCHAR(50),
    end_logradouro VARCHAR(255),
    end_tipo_logradouro VARCHAR(50),
    end_numero VARCHAR(50),
    end_bairro VARCHAR(255),
    end_cidade VARCHAR(255),
    end_estado VARCHAR(2),
    end_pais VARCHAR(255),
    end_complemento VARCHAR(255),
    end_apelido VARCHAR(255) NOT NULL,
    end_observacao TEXT,
    end_favorito BOOLEAN,
    end_cobranca BOOLEAN,
    cli_id BIGINT,
    FOREIGN KEY (cli_id) REFERENCES cli_cliente(cli_id) ON DELETE CASCADE
);

--
-- Criação da tabela de Cartões de Crédito
--
CREATE TABLE car_cartao (
    car_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    car_numero VARCHAR(16) NOT NULL,
    car_nome_impresso VARCHAR(255) NOT NULL,
    car_cvv VARCHAR(4) NOT NULL,
    car_bandeira VARCHAR(50),
    car_validade DATE NOT NULL,
    car_apelido VARCHAR(255),
    car_favorito BOOLEAN,
    cli_id BIGINT,
    FOREIGN KEY (cli_id) REFERENCES cli_cliente(cli_id) ON DELETE CASCADE
);

--
-- Criação da tabela de Grupo de Precificação
--
CREATE TABLE grp_grupo_precificacao (
    grp_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    grp_nome VARCHAR(255) NOT NULL,
    grp_piso_preco DOUBLE,
    grp_teto_preco DOUBLE,
    grp_percentual_margem DOUBLE NOT NULL
);

--
-- Criação da tabela de Categorias
--
CREATE TABLE cat_categoria (
    cat_id        BIGINT       PRIMARY KEY AUTO_INCREMENT,
    cat_nome      VARCHAR(255) NOT NULL,
    cat_descricao TEXT
);

--
-- Criação da tabela de Produtos
--
CREATE TABLE pro_produto (
    pro_id           BIGINT       PRIMARY KEY AUTO_INCREMENT,
    pro_nome         VARCHAR(255) NOT NULL,
    pro_descricao    TEXT,
    pro_peso         DOUBLE,
    pro_aroma        VARCHAR(100),
    pro_ingredientes TEXT,
    pro_indicacao    VARCHAR(100),
    pro_foto         VARCHAR(500),
    pro_dimensoes    VARCHAR(50),
    pro_validade     VARCHAR(20),
    grp_id           BIGINT,
    cat_id           BIGINT,
    FOREIGN KEY (grp_id) REFERENCES grp_grupo_precificacao(grp_id),
    FOREIGN KEY (cat_id) REFERENCES cat_categoria(cat_id)
);

--
-- Criação da tabela de Estoque
--
CREATE TABLE est_estoque (
    est_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pro_id BIGINT NOT NULL,
    est_quantidade INT NOT NULL,
    est_valor_custo DECIMAL(10, 2) NOT NULL,
    est_valor_venda DECIMAL(10, 2) NOT NULL,
    est_fornecedor VARCHAR(255) NOT NULL,
    est_data_entrada DATE NOT NULL,
    FOREIGN KEY (pro_id) REFERENCES pro_produto(pro_id)
);

--
-- Criação da tabela de Vendas
--
CREATE TABLE ven_venda (
    ven_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cli_id BIGINT NOT NULL,
    ven_data DATE NOT NULL,
    ven_status VARCHAR(50) NOT NULL DEFAULT 'EM_PROCESSAMENTO',
    ven_frete DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    ven_valor_total DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (cli_id) REFERENCES cli_cliente(cli_id)
);
-- Migração para bancos existentes:
-- ALTER TABLE ven_venda ADD COLUMN ven_frete DECIMAL(10, 2) NOT NULL DEFAULT 0.00;

--
-- Criação da tabela de Itens de Venda
--
CREATE TABLE ven_item_venda (
    ven_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ven_id BIGINT NOT NULL,
    pro_id BIGINT NOT NULL,
    ven_item_quantidade INT NOT NULL,
    ven_item_valor_unitario DECIMAL(10, 2) NOT NULL,
    ven_item_valor_total DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (ven_id) REFERENCES ven_venda(ven_id) ON DELETE CASCADE,
    FOREIGN KEY (pro_id) REFERENCES pro_produto(pro_id)
);

-- =============================================================
-- Seed: Categorias e Grupos de Precificação
-- Saboria Natural - E-commerce de Sabonetes Artesanais
-- =============================================================

-- -------------------------------------------------------------
-- Grupos de Precificação
-- -------------------------------------------------------------
INSERT INTO grp_grupo_precificacao (grp_nome, grp_piso_preco, grp_teto_preco, grp_percentual_margem) VALUES
('Linha Básica',    8.00,  20.00, 80.00),
('Linha Premium',  18.00,  60.00, 120.00),
('Linha Especial', 30.00, 100.00, 150.00);

-- -------------------------------------------------------------
-- Categorias
-- -------------------------------------------------------------
INSERT INTO cat_categoria (cat_nome, cat_descricao) VALUES
('Florais',        'Sabonetes com essências de flores como lavanda, rosa e jasmin'),
('Cítricos',       'Sabonetes refrescantes com limão, laranja e tangerina'),
('Terrosos',       'Sabonetes com argila, café e elementos da terra'),
('Especiarias',    'Sabonetes com canela, cravo, gengibre e outras especiarias'),
('Herbais',        'Sabonetes com ervas medicinais como camomila, hortelã e alecrim'),
('Neutros',        'Sabonetes sem fragrância, indicados para pele sensível');


CREATE TABLE ava_avaliacao (
      ava_id             BIGINT       PRIMARY KEY AUTO_INCREMENT,                                            cli_id             BIGINT       NOT NULL,
      pro_id             BIGINT       NOT NULL,                                                        
      ava_nota           INT          NOT NULL,
      ava_descricao      TEXT,
      ava_data_avaliacao DATE         NOT NULL,
      ava_data_alteracao DATE,
      FOREIGN KEY (cli_id) REFERENCES cli_cliente(cli_id),
      FOREIGN KEY (pro_id) REFERENCES pro_produto(pro_id),
      UNIQUE KEY uk_cli_pro (cli_id, pro_id)
  );


CREATE TABLE ven_pagamento_venda (                             
    pag_id    BIGINT       NOT NULL AUTO_INCREMENT,              
    ven_id    BIGINT       NOT NULL,                             
    car_id    BIGINT       NOT NULL,                             
    pag_valor DECIMAL(10,2) NOT NULL,                            
    PRIMARY KEY (pag_id),
    CONSTRAINT fk_pag_venda   FOREIGN KEY (ven_id) REFERENCES    
  ven_venda(ven_id),                                        
    CONSTRAINT fk_pag_cartao  FOREIGN KEY (car_id) REFERENCES
  car_cartao(car_id)
  );