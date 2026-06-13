/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.cecilia.gigi.strategies;

import java.sql.SQLException;

import com.cecilia.gigi.dominio.EntidadeDominio;

/**
 *
 * @author marco
 */
public interface IStrategy {
    public Boolean processar(EntidadeDominio entidadeDominio) throws SQLException;
}
