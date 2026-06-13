package com.cecilia.gigi.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cecilia.gigi.dominio.Devolucao;
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.facades.DevolucaoFacade;

@RestController
@RequestMapping("/devolucoes")
public class DevolucaoController {

    private final DevolucaoFacade devolucaoFacade;

    public DevolucaoController() throws SQLException {
        this.devolucaoFacade = new DevolucaoFacade();
    }

    @PostMapping
    public ResponseEntity<Object> save(@RequestBody Devolucao devolucao) {
        try {
            devolucaoFacade.save(devolucao);
            return new ResponseEntity<>(devolucaoFacade.get(devolucao.getId()), HttpStatus.CREATED);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable Long id) {
        try {
            EntidadeDominio dev = devolucaoFacade.get(id);
            return dev != null
                    ? new ResponseEntity<>(dev, HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<Object> getAll(
            @RequestParam(defaultValue = "20") Long limit,
            @RequestParam(defaultValue = "0") Long offset) {
        try {
            return new ResponseEntity<>(devolucaoFacade.getAll(limit, offset), HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/venda/{vendaId}")
    public ResponseEntity<Object> getByVendaId(@PathVariable Long vendaId) {
        try {
            List<Devolucao> devs = devolucaoFacade.getByVendaId(vendaId);
            return new ResponseEntity<>(devs, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/autorizar")
    public ResponseEntity<Object> autorizar(@PathVariable Long id) {
        try {
            devolucaoFacade.autorizar(id);
            return new ResponseEntity<>(devolucaoFacade.get(id), HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/concluir")
    public ResponseEntity<Object> concluir(@PathVariable Long id) {
        try {
            devolucaoFacade.concluir(id);
            return new ResponseEntity<>(devolucaoFacade.get(id), HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
