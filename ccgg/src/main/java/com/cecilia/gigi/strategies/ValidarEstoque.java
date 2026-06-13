package com.cecilia.gigi.strategies;

import java.sql.SQLException;

import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.dominio.Estoque;

public class ValidarEstoque implements IStrategy {

    @Override
    public Boolean processar(EntidadeDominio entidadeDominio) throws SQLException {
        Estoque estoque = (Estoque) entidadeDominio;

        if (estoque.getProduto() == null || estoque.getProduto().getId() == null) {
            throw new SQLException("{\"error\": \"Produto é obrigatório\", \"status\": 400}");
        }

        if (estoque.getQuantidade() == null || estoque.getQuantidade() <= 0) {
            throw new SQLException("{\"error\": \"Quantidade deve ser maior que zero\", \"status\": 400}");
        }

        if (estoque.getValorCusto() == null || estoque.getValorCusto() <= 0) {
            throw new SQLException("{\"error\": \"Valor de custo é obrigatório e deve ser maior que zero\", \"status\": 400}");
        }

        if (estoque.getFornecedor() == null || estoque.getFornecedor().trim().isEmpty()) {
            throw new SQLException("{\"error\": \"Fornecedor é obrigatório\", \"status\": 400}");
        }

        if (estoque.getDataEntrada() == null) {
            throw new SQLException("{\"error\": \"Data de entrada é obrigatória\", \"status\": 400}");
        }

        return true;
    }
}
