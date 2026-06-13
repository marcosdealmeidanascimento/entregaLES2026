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

import com.cecilia.gigi.dominio.Avaliacao;
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.facades.AvaliacaoFacade;

@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoFacade avaliacaoFacade;

    public AvaliacaoController() throws SQLException {
        this.avaliacaoFacade = new AvaliacaoFacade();
    }

    @PostMapping
    public ResponseEntity<Object> save(@RequestBody Avaliacao avaliacao) {
        try {
            avaliacaoFacade.save(avaliacao);
            EntidadeDominio saved = avaliacaoFacade.get(avaliacao.getId());
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable Long id) {
        try {
            EntidadeDominio avaliacao = avaliacaoFacade.get(id);
            if (avaliacao != null) {
                return new ResponseEntity<>(avaliacao, HttpStatus.OK);
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
            List<EntidadeDominio> avaliacoes = avaliacaoFacade.getAll(limit, offset);
            return new ResponseEntity<>(avaliacoes, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody Avaliacao avaliacao) {
        try {
            avaliacaoFacade.update(id, avaliacao);
            EntidadeDominio updated = avaliacaoFacade.get(id);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        try {
            avaliacaoFacade.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<Object> getByProdutoId(
            @PathVariable Long produtoId,
            @RequestParam(defaultValue = "10") Long limit,
            @RequestParam(defaultValue = "0") Long offset) {
        try {
            List<EntidadeDominio> avaliacoes = avaliacaoFacade.getByProdutoId(produtoId, limit, offset);
            return new ResponseEntity<>(avaliacoes, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<Object> getByClienteId(
            @PathVariable Long clienteId,
            @RequestParam(defaultValue = "10") Long limit,
            @RequestParam(defaultValue = "0") Long offset) {
        try {
            List<EntidadeDominio> avaliacoes = avaliacaoFacade.getByClienteId(clienteId, limit, offset);
            return new ResponseEntity<>(avaliacoes, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
