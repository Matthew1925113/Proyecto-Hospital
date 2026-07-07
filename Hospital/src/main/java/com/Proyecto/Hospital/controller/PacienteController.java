package com.Proyecto.Hospital.controller;

import com.Proyecto.Hospital.Model.Paciente;
import com.Proyecto.Hospital.Service.PacienteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @GetMapping({"/", "/listaPacientes"})
    public String index(Model model) {
        model.addAttribute("listaPacientes", service.ListarPacientes());
        return "ListaPacientes";
    }

    @GetMapping({"/pacienteNuevo", "/FormularioPaciente"})
    public String newPaciente(Model model) {
        model.addAttribute("paciente", new Paciente());
        return "FormularioPaciente";
    }

    @GetMapping({"/EditarPaciente/{id}", "/editarPaciente/{id}"})
    public String editPaciente(@PathVariable Long id, Model model) {
        Paciente paciente = service.ObtenerPaciente(id);
        if (paciente == null) {
            return "redirect:/listaPacientes";
        }
        model.addAttribute("paciente", paciente);
        return "FormularioPaciente";
    }

    @PostMapping("/GuardarPaciente")
    public String updatePaciente(@ModelAttribute Paciente paciente) {
        service.GuardarPaciente(paciente);
        return "redirect:/listaPacientes";
    }

    @GetMapping({"/eliminarPaciente", "/EliminarPaciente"})
    public String deletePaciente(@RequestParam Long id) {
        service.EliminarPaciente(id);
        return "redirect:/listaPacientes";
    }
}