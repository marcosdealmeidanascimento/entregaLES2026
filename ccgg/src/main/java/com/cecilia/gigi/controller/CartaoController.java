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

import com.cecilia.gigi.dominio.Cartao;
import com.cecilia.gigi.dominio.EntidadeDominio;
import com.cecilia.gigi.facades.CartaoFacade;

/**
 *
 * @author marco
 */
@RestController
@RequestMapping("/cartoes")
public class CartaoController {

    private final CartaoFacade cartaoFacade;

    public CartaoController() throws SQLException {
        this.cartaoFacade = new CartaoFacade();
    }

    @PostMapping
    public ResponseEntity<Object> save(@RequestBody Cartao cartao) {
        try {
            cartaoFacade.save(cartao);
            return new ResponseEntity<>(cartao, HttpStatus.CREATED);
        } catch (SQLException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable Long id) {
        try {
            EntidadeDominio cartao = cartaoFacade.get(id);
            if (cartao != null) {
                return new ResponseEntity<>(cartao, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (SQLException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<EntidadeDominio>> getAll(
        @RequestParam(defaultValue = "10") Long limit,
        @RequestParam(defaultValue = "0") Long offset) {
        try {
            List<EntidadeDominio> cartoes = cartaoFacade.getAll(limit, offset);
            return new ResponseEntity<>(cartoes, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody Cartao cartao) {
        try {
            cartaoFacade.update(id, cartao);
            return new ResponseEntity<>(cartao, HttpStatus.OK);
        } catch (SQLException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        try {
            cartaoFacade.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (SQLException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }
}