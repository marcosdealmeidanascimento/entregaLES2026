package com.cecilia.gigi.strategies;

import java.sql.SQLException;

import com.cecilia.gigi.dao.AvaliacaoDAO;
import com.cecilia.gigi.dominio.Avaliacao;
import com.cecilia.gigi.dominio.EntidadeDominio;

public class ValidarAvaliacao implements IStrategy {

    private final AvaliacaoDAO avaliacaoDAO;

    public ValidarAvaliacao() throws SQLException {
        this.avaliacaoDAO = new AvaliacaoDAO();
    }

    @Override
    public Boolean processar(EntidadeDominio entidadeDominio) throws SQLException {
        Avaliacao avaliacao = (Avaliacao) entidadeDominio;

        if (avaliacao.getCliente() == null || avaliacao.getCliente().getId() == null) {
            throw new SQLException("{\"error\": \"Cliente é obrigatório\", \"status\": 400}");
        }

        if (avaliacao.getProduto() == null || avaliacao.getProduto().getId() == null) {
            throw new SQLException("{\"error\": \"Produto é obrigatório\", \"status\": 400}");
        }

        if (avaliacao.getNota() == null || avaliacao.getNota() < 1 || avaliacao.getNota() > 5) {
            throw new SQLException("{\"error\": \"Nota deve ser um valor inteiro entre 1 e 5\", \"status\": 400}");
        }

        if (!avaliacaoDAO.clienteElegivel(avaliacao.getCliente().getId(), avaliacao.getProduto().getId())) {
            throw new SQLException("{\"error\": \"Cliente não possui compra entregue para este produto\", \"status\": 422}");
        }

        if (avaliacaoDAO.existeAvaliacao(avaliacao.getCliente().getId(), avaliacao.getProduto().getId())) {
            throw new SQLException("{\"error\": \"Cliente já avaliou este produto\", \"status\": 409}");
        }

        return true;
    }
}
