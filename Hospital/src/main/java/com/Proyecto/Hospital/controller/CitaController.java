package com.Proyecto.Hospital.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CitaController {

    @GetMapping("/citas")
    public String citas() {
        return "citas";
    }
}