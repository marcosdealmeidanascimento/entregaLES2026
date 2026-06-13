package com.cecilia.gigi.facades;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.cecilia.gigi.dao.AvaliacaoDAO;
import com.cecilia.gigi.dominio.Avaliacao;
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.strategies.ValidarAvaliacao;

public class AvaliacaoFacade implements IFacade {

    private final AvaliacaoDAO avaliacaoDAO;
    private final ValidarAvaliacao validarAvaliacao;

    public AvaliacaoFacade() throws SQLException {
        this.avaliacaoDAO = new AvaliacaoDAO();
        this.validarAvaliacao = new ValidarAvaliacao();
    }

    @Override
    public EntidadeDominio get(Long id) throws SQLException {
        return avaliacaoDAO.get(id);
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        return avaliacaoDAO.getAll(limit, offset);
    }

    @Override
    public List<EntidadeDominio> filter(EntidadeDominio entidadeDominio, Long limit, Long offset) throws SQLException {
        return avaliacaoDAO.filter(entidadeDominio, limit, offset);
    }

    @Override
    public Boolean save(EntidadeDominio entidadeDominio) throws SQLException {
        Avaliacao avaliacao = (Avaliacao) entidadeDominio;

        validarAvaliacao.processar(avaliacao);

        if (avaliacao.getDataAvaliacao() == null) {
            avaliacao.setDataAvaliacao(LocalDate.now());
        }

        avaliacaoDAO.save(avaliacao);
        return true;
    }

    @Override
    public Boolean update(Long id, EntidadeDominio entidadeDominio) throws SQLException {
        Avaliacao avaliacao = (Avaliacao) entidadeDominio;

        if (avaliacao.getNota() == null || avaliacao.getNota() < 1 || avaliacao.getNota() > 5) {
            throw new SQLException("{\"error\": \"Nota deve ser um valor inteiro entre 1 e 5\", \"status\": 400}");
        }

        avaliacaoDAO.update(id, avaliacao);
        return true;
    }

    @Override
    public Boolean delete(Long id) throws SQLException {
        avaliacaoDAO.delete(id);
        return true;
    }

    public List<EntidadeDominio> getByProdutoId(Long produtoId, Long limit, Long offset) throws SQLException {
        return avaliacaoDAO.getByProdutoId(produtoId, limit, offset);
    }

    public List<EntidadeDominio> getByClienteId(Long clienteId, Long limit, Long offset) throws SQLException {
        return avaliacaoDAO.getByClienteId(clienteId, limit, offset);
    }
}
