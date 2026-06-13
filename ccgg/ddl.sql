-- gigi.cat_categoria definition

CREATE TABLE `cat_categoria` (
  `cat_id` bigint NOT NULL AUTO_INCREMENT,
  `cat_nome` varchar(255) NOT NULL,
  `cat_descricao` text,
  PRIMARY KEY (`cat_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- gigi.cli_cliente definition

CREATE TABLE `cli_cliente` (
  `cli_id` bigint NOT NULL AUTO_INCREMENT,
  `cli_nome` varchar(255) NOT NULL,
  `cli_genero` varchar(50) DEFAULT NULL,
  `cli_cpf` varchar(14) NOT NULL,
  `cli_email` varchar(100) NOT NULL,
  `cli_telefone` varchar(20) DEFAULT NULL,
  `cli_tipo_telefone` varchar(50) DEFAULT NULL,
  `cli_telefone_ddd` varchar(5) DEFAULT NULL,
  `cli_data_nascimento` date DEFAULT NULL,
  `cli_ranking` bigint DEFAULT '0',
  `cli_status` tinyint(1) DEFAULT '1',
  `cli_senha` varchar(255) NOT NULL,
  `dat_data_cadastro` datetime DEFAULT CURRENT_TIMESTAMP,
  `dat_data_alteracao` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`cli_id`),
  UNIQUE KEY `cli_cpf` (`cli_cpf`),
  UNIQUE KEY `cli_email` (`cli_email`)
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- gigi.grp_grupo_precificacao definition

CREATE TABLE `grp_grupo_precificacao` (
  `grp_id` bigint NOT NULL AUTO_INCREMENT,
  `grp_nome` varchar(255) NOT NULL,
  `grp_piso_preco` double DEFAULT NULL,
  `grp_teto_preco` double DEFAULT NULL,
  `grp_percentual_margem` double NOT NULL,
  PRIMARY KEY (`grp_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- gigi.car_cartao definition

CREATE TABLE `car_cartao` (
  `car_id` bigint NOT NULL AUTO_INCREMENT,
  `car_numero` varchar(20) NOT NULL,
  `car_nome_impresso` varchar(100) NOT NULL,
  `car_cvv` varchar(4) NOT NULL,
  `car_bandeira` varchar(50) DEFAULT NULL,
  `car_validade` date NOT NULL,
  `car_apelido` varchar(100) DEFAULT NULL,
  `car_favorito` tinyint(1) DEFAULT '0',
  `cli_id` bigint NOT NULL,
  `dat_data_cadastro` datetime DEFAULT CURRENT_TIMESTAMP,
  `dat_data_alteracao` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`car_id`),
  KEY `fk_cartao_cliente` (`cli_id`),
  CONSTRAINT `fk_cartao_cliente` FOREIGN KEY (`cli_id`) REFERENCES `cli_cliente` (`cli_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=96 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- gigi.cup_cupom definition

CREATE TABLE `cup_cupom` (
  `cup_id` bigint NOT NULL AUTO_INCREMENT,
  `cup_codigo` varchar(50) NOT NULL,
  `cup_valor` double NOT NULL,
  `cup_tipo` varchar(20) NOT NULL COMMENT 'FIXED ou PERCENT',
  `cup_ativo` tinyint(1) NOT NULL DEFAULT '1',
  `cup_eh_troca` tinyint(1) NOT NULL DEFAULT '0',
  `cup_cliente_id` bigint DEFAULT NULL COMMENT 'NULL = cupom universal',
  `dat_data_cadastro` datetime DEFAULT CURRENT_TIMESTAMP,
  `dat_data_alteracao` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`cup_id`),
  UNIQUE KEY `cup_codigo` (`cup_codigo`),
  KEY `fk_cup_cliente` (`cup_cliente_id`),
  CONSTRAINT `fk_cup_cliente` FOREIGN KEY (`cup_cliente_id`) REFERENCES `cli_cliente` (`cli_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- gigi.end_endereco definition

CREATE TABLE `end_endereco` (
  `end_id` bigint NOT NULL AUTO_INCREMENT,
  `end_cep` varchar(10) NOT NULL,
  `end_tipo_residencia` varchar(50) DEFAULT NULL,
  `end_logradouro` varchar(255) NOT NULL,
  `end_tipo_logradouro` varchar(50) DEFAULT NULL,
  `end_numero` varchar(20) DEFAULT NULL,
  `end_bairro` varchar(100) DEFAULT NULL,
  `end_cidade` varchar(100) DEFAULT NULL,
  `end_estado` varchar(50) DEFAULT NULL,
  `end_pais` varchar(50) DEFAULT NULL,
  `end_complemento` varchar(255) DEFAULT NULL,
  `end_apelido` varchar(100) DEFAULT NULL,
  `end_observacao` text,
  `end_favorito` tinyint(1) DEFAULT '0',
  `end_cobranca` tinyint(1) DEFAULT '0',
  `cli_id` bigint NOT NULL,
  `dat_data_cadastro` datetime DEFAULT CURRENT_TIMESTAMP,
  `dat_data_alteracao` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`end_id`),
  KEY `fk_endereco_cliente` (`cli_id`),
  CONSTRAINT `fk_endereco_cliente` FOREIGN KEY (`cli_id`) REFERENCES `cli_cliente` (`cli_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=95 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- gigi.pro_produto definition

CREATE TABLE `pro_produto` (
  `pro_id` bigint NOT NULL AUTO_INCREMENT,
  `pro_nome` varchar(255) NOT NULL,
  `pro_descricao` text,
  `pro_peso` double DEFAULT NULL,
  `pro_aroma` varchar(100) DEFAULT NULL,
  `pro_ingredientes` text,
  `pro_indicacao` varchar(100) DEFAULT NULL,
  `pro_foto` varchar(500) DEFAULT NULL,
  `pro_dimensoes` varchar(50) DEFAULT NULL,
  `pro_validade` varchar(20) DEFAULT NULL,
  `grp_id` bigint DEFAULT NULL,
  `cat_id` bigint DEFAULT NULL,
  `pro_ativo` tinyint(1) NOT NULL DEFAULT '1',
  `dat_data_cadastro` datetime DEFAULT CURRENT_TIMESTAMP,
  `dat_data_alteracao` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`pro_id`),
  KEY `grp_id` (`grp_id`),
  KEY `cat_id` (`cat_id`),
  CONSTRAINT `pro_produto_ibfk_1` FOREIGN KEY (`grp_id`) REFERENCES `grp_grupo_precificacao` (`grp_id`),
  CONSTRAINT `pro_produto_ibfk_2` FOREIGN KEY (`cat_id`) REFERENCES `cat_categoria` (`cat_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- gigi.ven_venda definition

CREATE TABLE `ven_venda` (
  `ven_id` bigint NOT NULL AUTO_INCREMENT,
  `cli_id` bigint NOT NULL,
  `ven_data` date NOT NULL,
  `ven_status` varchar(50) NOT NULL DEFAULT 'EM_PROCESSAMENTO',
  `ven_valor_total` decimal(10,2) NOT NULL,
  `ven_frete` decimal(10,2) NOT NULL DEFAULT '0.00',
  `dat_data_cadastro` datetime DEFAULT CURRENT_TIMESTAMP,
  `dat_data_alteracao` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ven_id`),
  KEY `cli_id` (`cli_id`),
  CONSTRAINT `ven_venda_ibfk_1` FOREIGN KEY (`cli_id`) REFERENCES `cli_cliente` (`cli_id`)
) ENGINE=InnoDB AUTO_INCREMENT=97 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- gigi.ava_avaliacao definition

CREATE TABLE `ava_avaliacao` (
  `ava_id` bigint NOT NULL AUTO_INCREMENT,
  `cli_id` bigint NOT NULL,
  `pro_id` bigint NOT NULL,
  `ava_nota` int NOT NULL,
  `ava_descricao` text,
  `ava_data_avaliacao` date NOT NULL,
  `ava_data_alteracao` date DEFAULT NULL,
  `dat_data_cadastro` datetime DEFAULT CURRENT_TIMESTAMP,
  `dat_data_alteracao` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ava_id`),
  UNIQUE KEY `uk_cli_pro` (`cli_id`,`pro_id`),
  KEY `pro_id` (`pro_id`),
  CONSTRAINT `ava_avaliacao_ibfk_1` FOREIGN KEY (`cli_id`) REFERENCES `cli_cliente` (`cli_id`),
  CONSTRAINT `ava_avaliacao_ibfk_2` FOREIGN KEY (`pro_id`) REFERENCES `pro_produto` (`pro_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- gigi.dev_devolucao definition

CREATE TABLE `dev_devolucao` (
  `dev_id` bigint NOT NULL AUTO_INCREMENT,
  `dev_venda_id` bigint NOT NULL,
  `dev_status` varchar(30) NOT NULL DEFAULT 'SOLICITADA' COMMENT 'SOLICITADA | AUTORIZADA | CONCLUIDA',
  `dev_data_solicitacao` date NOT NULL,
  `dat_data_cadastro` datetime DEFAULT CURRENT_TIMESTAMP,
  `dat_data_alteracao` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`dev_id`),
  KEY `fk_dev_venda` (`dev_venda_id`),
  CONSTRAINT `fk_dev_venda` FOREIGN KEY (`dev_venda_id`) REFERENCES `ven_venda` (`ven_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- gigi.est_estoque definition

CREATE TABLE `est_estoque` (
  `est_id` bigint NOT NULL AUTO_INCREMENT,
  `pro_id` bigint NOT NULL,
  `est_quantidade` int NOT NULL,
  `est_valor_custo` decimal(10,2) NOT NULL,
  `est_valor_venda` decimal(10,2) NOT NULL,
  `est_fornecedor` varchar(255) NOT NULL,
  `est_data_entrada` date NOT NULL,
  `dat_data_cadastro` datetime DEFAULT CURRENT_TIMESTAMP,
  `dat_data_alteracao` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`est_id`),
  KEY `pro_id` (`pro_id`),
  CONSTRAINT `est_estoque_ibfk_1` FOREIGN KEY (`pro_id`) REFERENCES `pro_produto` (`pro_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- gigi.ven_cupom_venda definition

CREATE TABLE `ven_cupom_venda` (
  `vcv_id` bigint NOT NULL AUTO_INCREMENT,
  `vcv_venda_id` bigint NOT NULL,
  `vcv_cupom_id` bigint NOT NULL,
  `vcv_valor_desconto` double NOT NULL,
  PRIMARY KEY (`vcv_id`),
  KEY `fk_vcv_venda` (`vcv_venda_id`),
  KEY `fk_vcv_cupom` (`vcv_cupom_id`),
  CONSTRAINT `fk_vcv_cupom` FOREIGN KEY (`vcv_cupom_id`) REFERENCES `cup_cupom` (`cup_id`),
  CONSTRAINT `fk_vcv_venda` FOREIGN KEY (`vcv_venda_id`) REFERENCES `ven_venda` (`ven_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- gigi.ven_item_venda definition

CREATE TABLE `ven_item_venda` (
  `ven_item_id` bigint NOT NULL AUTO_INCREMENT,
  `ven_id` bigint NOT NULL,
  `pro_id` bigint NOT NULL,
  `ven_item_quantidade` int NOT NULL,
  `ven_item_valor_unitario` decimal(10,2) NOT NULL,
  `ven_item_valor_total` decimal(10,2) NOT NULL,
  PRIMARY KEY (`ven_item_id`),
  KEY `ven_id` (`ven_id`),
  KEY `pro_id` (`pro_id`),
  CONSTRAINT `ven_item_venda_ibfk_1` FOREIGN KEY (`ven_id`) REFERENCES `ven_venda` (`ven_id`) ON DELETE CASCADE,
  CONSTRAINT `ven_item_venda_ibfk_2` FOREIGN KEY (`pro_id`) REFERENCES `pro_produto` (`pro_id`)
) ENGINE=InnoDB AUTO_INCREMENT=112 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- gigi.ven_pagamento_venda definition

CREATE TABLE `ven_pagamento_venda` (
  `pag_id` bigint NOT NULL AUTO_INCREMENT,
  `ven_id` bigint NOT NULL,
  `car_id` bigint NOT NULL,
  `pag_valor` decimal(10,2) NOT NULL,
  PRIMARY KEY (`pag_id`),
  KEY `fk_pag_venda` (`ven_id`),
  KEY `fk_pag_cartao` (`car_id`),
  CONSTRAINT `fk_pag_cartao` FOREIGN KEY (`car_id`) REFERENCES `car_cartao` (`car_id`),
  CONSTRAINT `fk_pag_venda` FOREIGN KEY (`ven_id`) REFERENCES `ven_venda` (`ven_id`)
) ENGINE=InnoDB AUTO_INCREMENT=111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- gigi.dev_item_devolucao definition

CREATE TABLE `dev_item_devolucao` (
  `dit_id` bigint NOT NULL AUTO_INCREMENT,
  `dit_devolucao_id` bigint NOT NULL,
  `dit_item_venda_id` bigint NOT NULL,
  `dit_quantidade` int NOT NULL,
  PRIMARY KEY (`dit_id`),
  KEY `fk_dit_devolucao` (`dit_devolucao_id`),
  KEY `fk_dit_item_venda` (`dit_item_venda_id`),
  CONSTRAINT `fk_dit_devolucao` FOREIGN KEY (`dit_devolucao_id`) REFERENCES `dev_devolucao` (`dev_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_dit_item_venda` FOREIGN KEY (`dit_item_venda_id`) REFERENCES `ven_item_venda` (`ven_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;