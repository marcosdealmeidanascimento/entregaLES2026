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
import com.cecilia.gigi.dominio.Venda;
import com.cecilia.gigi.facades.VendaFacade;

@RestController
@RequestMapping("/vendas")
public class VendaController {

    private final VendaFacade vendaFacade;

    public VendaController() throws SQLException {
        this.vendaFacade = new VendaFacade();
    }

    @PostMapping
    public ResponseEntity<Object> save(@RequestBody Venda venda) {
        try {
            vendaFacade.save(venda);
            EntidadeDominio saved = vendaFacade.get(venda.getId());
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable Long id) {
        try {
            EntidadeDominio venda = vendaFacade.get(id);
            if (venda != null) {
                return new ResponseEntity<>(venda, HttpStatus.OK);
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
            List<EntidadeDominio> vendas = vendaFacade.getAll(limit, offset);
            return new ResponseEntity<>(vendas, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<Object> getByClienteId(@PathVariable Long clienteId) {
        try {
            List<EntidadeDominio> vendas = vendaFacade.getByClienteId(clienteId);
            return new ResponseEntity<>(vendas, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        try {
            vendaFacade.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/aprovar")
    public ResponseEntity<Object> aprovar(@PathVariable Long id) {
        try {
            vendaFacade.aprovar(id);
            return new ResponseEntity<>(vendaFacade.get(id), HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/reprovar")
    public ResponseEntity<Object> reprovar(@PathVariable Long id) {
        try {
            vendaFacade.reprovar(id);
            return new ResponseEntity<>(vendaFacade.get(id), HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/despachar")
    public ResponseEntity<Object> despachar(@PathVariable Long id) {
        try {
            vendaFacade.despachar(id);
            return new ResponseEntity<>(vendaFacade.get(id), HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/entregar")
    public ResponseEntity<Object> entregar(@PathVariable Long id) {
        try {
            vendaFacade.entregar(id);
            return new ResponseEntity<>(vendaFacade.get(id), HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/solicitar-troca")
    public ResponseEntity<Object> solicitarTroca(@PathVariable Long id) {
        try {
            vendaFacade.solicitarTroca(id);
            return new ResponseEntity<>(vendaFacade.get(id), HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/autorizar-troca")
    public ResponseEntity<Object> autorizarTroca(@PathVariable Long id) {
        try {
            vendaFacade.autorizarTroca(id);
            return new ResponseEntity<>(vendaFacade.get(id), HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/confirmar-troca")
    public ResponseEntity<Object> confirmarTroca(@PathVariable Long id) {
        try {
            vendaFacade.confirmarTroca(id);
            return new ResponseEntity<>(vendaFacade.get(id), HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
