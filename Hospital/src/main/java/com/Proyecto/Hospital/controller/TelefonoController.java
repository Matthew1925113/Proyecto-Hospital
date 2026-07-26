package com.Proyecto.Hospital.controller;

import com.Proyecto.Hospital.Service.TelefonoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TelefonoController {

    private final TelefonoService telefonoService;

    public TelefonoController(TelefonoService telefonoService) {
        this.telefonoService = telefonoService;
    }

    @PostMapping("/telefono/agregar/{pacienteId}")
    public String agregarTelefono(@PathVariable Long pacienteId, @RequestParam String numero) {
        telefonoService.agregarTelefono(pacienteId, numero);
        return "redirect:/EditarPaciente/" + pacienteId;
    }

    @PostMapping("/telefono/eliminar/{id}/{pacienteId}")
    public String eliminarTelefono(@PathVariable Long id, @PathVariable Long pacienteId) {
        telefonoService.eliminarTelefono(id);
        return "redirect:/EditarPaciente/" + pacienteId;
    }
}

