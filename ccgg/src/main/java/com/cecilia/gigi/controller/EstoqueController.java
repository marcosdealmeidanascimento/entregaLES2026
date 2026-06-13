package com.cecilia.gigi.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.dominio.Estoque;
import com.cecilia.gigi.facades.EstoqueFacade;

@RestController
@RequestMapping("/estoques")
public class EstoqueController {

    private final EstoqueFacade estoqueFacade;

    public EstoqueController() throws SQLException {
        this.estoqueFacade = new EstoqueFacade();
    }

    // RF0051: Realizar entrada em estoque
    @PostMapping
    public ResponseEntity<Object> save(@RequestBody Estoque estoque) {
        try {
            estoqueFacade.save(estoque);
            EntidadeDominio saved = estoqueFacade.get(estoque.getId());
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable Long id) {
        try {
            EntidadeDominio estoque = estoqueFacade.get(id);
            if (estoque != null) {
                return new ResponseEntity<>(estoque, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<EntidadeDominio>> getAll(
            @RequestParam(defaultValue = "10") Long limit,
            @RequestParam(defaultValue = "0") Long offset) {
        try {
            List<EntidadeDominio> estoques = estoqueFacade.getAll(limit, offset);
            return new ResponseEntity<>(estoques, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody Estoque estoque) {
        try {
            estoqueFacade.update(id, estoque);
            EntidadeDominio updated = estoqueFacade.get(id);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        try {
            estoqueFacade.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // RF0053: Dar baixa em estoque
    @PostMapping("/baixa")
    public ResponseEntity<Object> darBaixa(
            @RequestParam Long produtoId,
            @RequestParam Integer quantidade) {
        try {
            estoqueFacade.darBaixa(produtoId, quantidade);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // RF0054: Reentrada em estoque (troca)
    @PostMapping("/reentrada")
    public ResponseEntity<Object> reentrada(
            @RequestParam Long produtoId,
            @RequestParam Integer quantidade) {
        try {
            estoqueFacade.reentrada(produtoId, quantidade);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Consulta quantidade disponível
    @GetMapping("/disponivel")
    public ResponseEntity<Object> getQuantidadeDisponivel(@RequestParam Long produtoId) {
        try {
            Integer quantidade = estoqueFacade.getQuantidadeDisponivel(produtoId);
            return new ResponseEntity<>(quantidade, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
