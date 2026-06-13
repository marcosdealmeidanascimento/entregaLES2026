/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.cecilia.gigi.dao;

import java.sql.SQLException;
import java.util.List;

import com.cecilia.gigi.dominio.EntidadeDominio;

/**
 *
 * @author marco
 */
public interface IDAO {
    EntidadeDominio get(Long id) throws SQLException;

    List<EntidadeDominio> getAll(Long limit, Long offset) throws SQLException;

    List<EntidadeDominio> filter(EntidadeDominio entidadeDominio, Long limit, Long offset) throws SQLException;

    EntidadeDominio save(EntidadeDominio entidadeDominio) throws SQLException;

    EntidadeDominio update(Long id, EntidadeDominio entidadeDominio) throws SQLException;

    EntidadeDominio delete(Long id) throws SQLException;
}
