package com.cecilia.gigi.strategies;

import java.sql.SQLException;

import com.cecilia.gigi.dao.CupomDAO;
import com.cecilia.gigi.dao.EstoqueDAO;
import com.cecilia.gigi.dominio.Cupom;
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.dominio.ItemVenda;
import com.cecilia.gigi.dominio.PagamentoVenda;
import com.cecilia.gigi.dominio.Venda;

public class ValidarVenda implements IStrategy {

    private final EstoqueDAO estoqueDAO;
    private final CupomDAO cupomDAO;

    public ValidarVenda() throws SQLException {
        this.estoqueDAO = new EstoqueDAO();
        this.cupomDAO = new CupomDAO();
    }

    @Override
    public Boolean processar(EntidadeDominio entidadeDominio) throws SQLException {
        Venda venda = (Venda) entidadeDominio;

        if (venda.getCliente() == null || venda.getCliente().getId() == null) {
            throw new SQLException("{\"error\": \"Cliente é obrigatório\", \"status\": 400}");
        }

        if (venda.getItens() == null || venda.getItens().isEmpty()) {
            throw new SQLException("{\"error\": \"A venda deve conter pelo menos um item\", \"status\": 400}");
        }

        for (ItemVenda item : venda.getItens()) {
            if (item.getProduto() == null || item.getProduto().getId() == null) {
                throw new SQLException("{\"error\": \"Produto é obrigatório em cada item\", \"status\": 400}");
            }
            if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
                throw new SQLException("{\"error\": \"Quantidade do item deve ser maior que zero\", \"status\": 400}");
            }

            Integer disponivel = estoqueDAO.getQuantidadeDisponivelByProduto(item.getProduto().getId());
            if (disponivel < item.getQuantidade()) {
                throw new SQLException("{\"error\": \"Estoque insuficiente para o produto ID " + item.getProduto().getId() +
                        ". Disponível: " + disponivel + ", Solicitado: " + item.getQuantidade() + "\", \"status\": 422}");
            }
        }

        if (venda.getPagamentos() == null || venda.getPagamentos().isEmpty()) {
            throw new SQLException("{\"error\": \"A venda deve conter pelo menos um pagamento\", \"status\": 400}");
        }

        for (PagamentoVenda pagamento : venda.getPagamentos()) {
            if (pagamento.getCartao() == null || pagamento.getCartao().getId() == null) {
                throw new SQLException("{\"error\": \"Cartão é obrigatório em cada pagamento\", \"status\": 400}");
            }
            if (pagamento.getValor() == null || pagamento.getValor() < 10.0) {
                throw new SQLException("{\"error\": \"Valor mínimo por cartão é R$ 10,00\", \"status\": 400}");
            }
        }

        // Validar cupons
        if (venda.getCupons() != null && !venda.getCupons().isEmpty()) {
            int promoCount = 0;
            for (Cupom cupomReq : venda.getCupons()) {
                Cupom cupom = cupomDAO.findByCodigo(cupomReq.getCodigo());
                if (cupom == null) {
                    throw new SQLException("{\"error\": \"Cupom '" + cupomReq.getCodigo() + "' não encontrado ou inativo\", \"status\": 404}");
                }
                if (cupom.getClienteId() != null && !cupom.getClienteId().equals(venda.getCliente().getId())) {
                    throw new SQLException("{\"error\": \"Cupom '" + cupom.getCodigo() + "' não pertence a este cliente\", \"status\": 403}");
                }
                if (!cupom.isEhTroca()) promoCount++;
                if (promoCount > 1) {
                    throw new SQLException("{\"error\": \"Apenas um cupom promocional é permitido por compra\", \"status\": 422}");
                }
                // Preenche os dados completos do cupom no objeto da venda
                cupomReq.setId(cupom.getId());
                cupomReq.setValor(cupom.getValor());
                cupomReq.setTipo(cupom.getTipo());
                cupomReq.setEhTroca(cupom.isEhTroca());
            }
        }

        return true;
    }
}
