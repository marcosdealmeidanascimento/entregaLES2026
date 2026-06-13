package com.cecilia.gigi.controller;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cecilia.gigi.dao.RelatorioDAO;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    private final RelatorioDAO relatorioDAO;

    public RelatorioController() throws SQLException {
        this.relatorioDAO = new RelatorioDAO();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Object> getDashboardStats() {
        try {
            return new ResponseEntity<>(relatorioDAO.getDashboardStats(), HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/vendas-diarias")
    public ResponseEntity<Object> getVendasDiarias(
            @RequestParam(defaultValue = "30") int dias) {
        try {
            if (dias <= 0 || dias > 365) dias = 30;
            return new ResponseEntity<>(relatorioDAO.getVendasDiarias(dias), HttpStatus.OK);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/vendas")
    public ResponseEntity<Object> getVendasPorPeriodo(
            @RequestParam String dataInicio,
            @RequestParam String dataFim,
            @RequestParam(defaultValue = "CATEGORIA") String agruparPor) {
        try {
            LocalDate inicio = LocalDate.parse(dataInicio);
            LocalDate fim = LocalDate.parse(dataFim);

            if (inicio.isAfter(fim)) {
                return new ResponseEntity<>(
                        "{\"error\": \"dataInicio deve ser anterior a dataFim\", \"status\": 400}",
                        HttpStatus.BAD_REQUEST);
            }

            Map<String, Object> resultado = relatorioDAO.getVendasPorPeriodo(inicio, fim, agruparPor);
            return new ResponseEntity<>(resultado, HttpStatus.OK);
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>(
                    "{\"error\": \"Formato de data inválido. Use YYYY-MM-DD\", \"status\": 400}",
                    HttpStatus.BAD_REQUEST);
        } catch (SQLException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
