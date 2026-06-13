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

import com.cecilia.gigi.dominio.Cupom;
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.facades.CupomFacade;

@RestController
@RequestMapping("/cupons")
public class CupomController {

    private final CupomFacade cupomFacade;

    public CupomController() throws SQLException {
        this.cupomFacade = new CupomFacade();
    }

    @PostMapping
    public ResponseEntity<Object> save(@RequestBody Cupom cupom) {
        try {
            cupomFacade.save(cupom);
            return new ResponseEntity<>(cupomFacade.get(cupom.getId()), HttpStatus.CREATED);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable Long id) {
        try {
            EntidadeDominio cupom = cupomFacade.get(id);
            return cupom != null
                    ? new ResponseEntity<>(cupom, HttpStatus.OK)
                    : new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<Object> getAll(
            @RequestParam(defaultValue = "50") Long limit,
            @RequestParam(defaultValue = "0") Long offset,
            @RequestParam(required = false) Long clienteId) {
        try {
            if (clienteId != null) {
                List<Cupom> disponiveis = cupomFacade.findDisponivelByCliente(clienteId);
                return new ResponseEntity<>(disponiveis, HttpStatus.OK);
            }
            return new ResponseEntity<>(cupomFacade.getAll(limit, offset), HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<Object> findByCodigo(@PathVariable String codigo) {
        try {
            Cupom cupom = cupomFacade.findByCodigo(codigo);
            return cupom != null
                    ? new ResponseEntity<>(cupom, HttpStatus.OK)
                    : new ResponseEntity<>("{\"error\": \"Cupom não encontrado ou inativo\"}", HttpStatus.NOT_FOUND);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody Cupom cupom) {
        try {
            cupomFacade.update(id, cupom);
            return new ResponseEntity<>(cupomFacade.get(id), HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        try {
            cupomFacade.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
