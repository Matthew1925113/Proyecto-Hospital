package com.Proyecto.Hospital.controller;

import com.Proyecto.Hospital.Model.Medico;
import com.Proyecto.Hospital.Service.MedicoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MedicoController {

    private final MedicoService service;

    public MedicoController(MedicoService service) {
        this.service = service;
    }

    @GetMapping({"/", "/listaMedicos"})
    public String listaMedicos(Model model) {
        model.addAttribute("medicos", service.ListarMedicos());
        return "listaMedicos";
    }

    @GetMapping({"/nuevoMedico", "/NuevoMedico"})
    public String nuevoMedicoForm(Model model) {
        model.addAttribute("medico", new Medico());
        return "formularioMedico";
    }

    @PostMapping({"/guardarMedico", "/GuardarMedico"})
    public String guardarMedico(Medico medico) {
        service.GuardarMedico(medico);
        return "redirect:/listaMedicos";
    }

    @GetMapping({"/editarMedico", "/EditarMedico"})
    public String editarMedicoForm(@RequestParam Long id, Model model) {
        Medico medico = service.ObtenerMedico(id);
        if (medico == null) {
            return "redirect:/listaMedicos";
        }
        model.addAttribute("medico", medico);
        return "formularioMedico";
    }

    @GetMapping({"/eliminarMedico", "/EliminarMedico"})
    public String deleteMedico(@RequestParam Long id) {
        service.EliminarMedico(id);
        return "redirect:/listaMedicos";
    }

    @GetMapping("/api/medicos")
    @ResponseBody
    public Iterable<Medico> listarMedicosJson() {
        return service.ListarMedicos();
    }
}