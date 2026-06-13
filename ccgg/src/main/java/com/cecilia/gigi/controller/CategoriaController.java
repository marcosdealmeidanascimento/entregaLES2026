package com.cecilia.gigi.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cecilia.gigi.dao.CategoriaDAO;
import com.cecilia.gigi.dominio.Categoria;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaDAO categoriaDAO;

    public CategoriaController() throws SQLException {
        this.categoriaDAO = new CategoriaDAO();
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        try {
            List<Categoria> categorias = categoriaDAO.getAll();
            return new ResponseEntity<>(categorias, HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
