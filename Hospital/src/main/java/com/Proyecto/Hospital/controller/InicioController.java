package com.Proyecto.Hospital.controller;

import com.Proyecto.Hospital.Service.CitaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {
    
    private final CitaService citaService;
    
    public InicioController(CitaService citaService) {
        this.citaService = citaService;
    }
    
    @GetMapping("/inicio")
    public String inicio(Model model){
        // Estadísticas de citas para el card de ADMIN
        var todasCitas = citaService.listarTodos();
        model.addAttribute("totalCitas", todasCitas.size());
        model.addAttribute("citasPendientes", todasCitas.stream()
            .filter(c -> "PENDIENTE".equals(c.getEstado())).count());
        model.addAttribute("citasConfirmadas", todasCitas.stream()
            .filter(c -> "CONFIRMADA".equals(c.getEstado())).count());
            
        return "inicio";
    }
}
