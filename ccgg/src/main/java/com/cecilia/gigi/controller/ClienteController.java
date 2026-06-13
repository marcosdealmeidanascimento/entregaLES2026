/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

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

import com.cecilia.gigi.dominio.Cliente;
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.facades.ClienteFacade;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteFacade clienteFacade;

    public ClienteController() throws SQLException {
        this.clienteFacade = new ClienteFacade();
    }

    @PostMapping
    public ResponseEntity<Object> save(@RequestBody Cliente cliente) {
        try {
            clienteFacade.save(cliente);
            EntidadeDominio savedCliente = clienteFacade.get(cliente.getId());
            return new ResponseEntity<>(savedCliente, HttpStatus.CREATED);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable Long id) {
        try {
            EntidadeDominio cliente = clienteFacade.get(id);
            if (cliente != null) {
                return new ResponseEntity<>(cliente, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<EntidadeDominio>> getAll(
        @RequestParam(defaultValue = "10") Long limit,
        @RequestParam(defaultValue = "0") Long offset) {
        try {
            List<EntidadeDominio> clientes = clienteFacade.getAll(limit, offset);
            return new ResponseEntity<>(clientes, HttpStatus.OK);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody Cliente cliente) {
        try {
            clienteFacade.update(id, cliente);
            EntidadeDominio updatedCliente = clienteFacade.get(id);
            return new ResponseEntity<>(updatedCliente, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        try {
            clienteFacade.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Cliente entidadeDominio) {
        try {
            EntidadeDominio loggedCliente = clienteFacade.login(entidadeDominio);
            if (loggedCliente == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(loggedCliente, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}