package com.cecilia.gigi.facades;

import java.sql.SQLException;
import java.util.List;

import com.cecilia.gigi.dao.CupomDAO;
import com.cecilia.gigi.dominio.Cupom;
import com.cecilia.gigi.dominio.EntidadeDominio;

public class CupomFacade implements IFacade {

    private final CupomDAO cupomDAO;

    public CupomFacade() throws SQLException {
        this.cupomDAO = new CupomDAO();
    }

    @Override
    public EntidadeDominio get(Long id) throws SQLException {
        return cupomDAO.get(id);
    }

    @Override
    public List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException {
        return cupomDAO.getAll(limit, offset);
    }

    @Override
    public List<EntidadeDominio> filter(EntidadeDominio entidadeDominio, Long limit, Long offset) throws SQLException {
        return cupomDAO.filter(entidadeDominio, limit, offset);
    }

    @Override
    public Boolean save(EntidadeDominio entidadeDominio) throws SQLException {
        Cupom cupom = (Cupom) entidadeDominio;
        if (cupom.getCodigo() == null || cupom.getCodigo().isBlank()) {
            throw new SQLException("{\"error\": \"Código do cupom é obrigatório\", \"status\": 400}");
        }
        if (cupom.getValor() == null || cupom.getValor() <= 0) {
            throw new SQLException("{\"error\": \"Valor do cupom deve ser maior que zero\", \"status\": 400}");
        }
        if (cupom.getTipo() == null || (!cupom.getTipo().equals("FIXED") && !cupom.getTipo().equals("PERCENT"))) {
            throw new SQLException("{\"error\": \"Tipo do cupom deve ser FIXED ou PERCENT\", \"status\": 400}");
        }
        cupomDAO.save(cupom);
        return true;
    }

    @Override
    public Boolean update(Long id, EntidadeDominio entidadeDominio) throws SQLException {
        cupomDAO.update(id, entidadeDominio);
        return true;
    }

    @Override
    public Boolean delete(Long id) throws SQLException {
        cupomDAO.desativar(id);
        return true;
    }

    public Cupom findByCodigo(String codigo) throws SQLException {
        return cupomDAO.findByCodigo(codigo);
    }

    public List<Cupom> findDisponivelByCliente(Long clienteId) throws SQLException {
        return cupomDAO.findDisponivelByCliente(clienteId);
    }
}
