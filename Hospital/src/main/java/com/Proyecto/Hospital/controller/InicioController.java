package com.Proyecto.Hospital.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {

    /*
     * ============================================================
     * RAÍZ DEL SISTEMA
     * ============================================================
     *
     * Si el usuario entra simplemente a:
     *
     * http://localhost:8080/
     *
     * lo enviamos al panel principal.
     */
    @GetMapping("/")
    public String raiz() {

        return "redirect:/inicio";
    }


    /*
     * ============================================================
     * PANEL PRINCIPAL
     * ============================================================
     */
    @GetMapping("/inicio")
    public String inicio() {

        return "inicio";
    }
}