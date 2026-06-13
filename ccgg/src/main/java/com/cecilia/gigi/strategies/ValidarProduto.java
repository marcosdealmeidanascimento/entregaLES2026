package com.cecilia.gigi.strategies;

import java.sql.SQLException;

import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.dominio.Produto;

public class ValidarProduto implements IStrategy {

    @Override
    public Boolean processar(EntidadeDominio entidadeDominio) throws SQLException {
        Produto produto = (Produto) entidadeDominio;

        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            throw new SQLException("{\"error\": \"Nome do produto é obrigatório\", \"status\": 400}");
        }

        return true;
    }
}
