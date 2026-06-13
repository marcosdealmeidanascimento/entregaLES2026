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
import com.cecilia.gigi.dominio.Produto;
import com.cecilia.gigi.facades.ProdutoFacade;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoFacade produtoFacade;

    public ProdutoController() throws SQLException {
        this.produtoFacade = new ProdutoFacade();
    }

    @PostMapping
    public ResponseEntity<Object> save(@RequestBody Produto produto) {
        try {
            produtoFacade.save(produto);
            EntidadeDominio saved = produtoFacade.get(produto.getId());
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable Long id) {
        try {
            EntidadeDominio produto = produtoFacade.get(id);
            if (produto != null) {
                return new ResponseEntity<>(produto, HttpStatus.OK);
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
            List<EntidadeDominio> produtos = produtoFacade.getAll(limit, offset);
            return new ResponseEntity<>(produtos, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody Produto produto) {
        try {
            produtoFacade.update(id, produto);
            EntidadeDominio updated = produtoFacade.get(id);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        try {
            produtoFacade.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
