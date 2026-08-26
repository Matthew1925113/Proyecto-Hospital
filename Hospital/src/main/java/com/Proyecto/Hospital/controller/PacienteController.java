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

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PacienteController {

    private final PacienteService service;


    public PacienteController(
        PacienteService service
    ) {

        this.service = service;
    }


    /*
     * ============================================================
     * LISTAR PACIENTES
     * ============================================================
     *
     * Antes este método también utilizaba:
     *
     * "/"
     *
     * Eso podía provocar conflictos con otros controladores.
     *
     * Ahora la lista administrativa de pacientes solamente
     * responde a:
     *
     * /listaPacientes
     */
    @GetMapping("/listaPacientes")
    public String index(
        Model model
    ) {

        model.addAttribute(
            "listaPacientes",
            service.ListarPacientes()
        );


        return "ListaPacientes";
    }


    /*
     * ============================================================
     * NUEVO PACIENTE
     * ============================================================
     */
    @GetMapping({
        "/pacienteNuevo",
        "/FormularioPaciente"
    })
    public String newPaciente(
        Model model
    ) {

        model.addAttribute(
            "paciente",
            new Paciente()
        );


        return "FormularioPaciente";
    }


    /*
     * ============================================================
     * EDITAR PACIENTE
     * ============================================================
     */
    @GetMapping({
        "/EditarPaciente/{id}",
        "/editarPaciente/{id}"
    })
    public String editPaciente(
        @PathVariable Long id,
        Model model
    ) {

        Paciente paciente =
            service.ObtenerPaciente(
                id
            );


        if (
            paciente == null
        ) {

            return "redirect:/listaPacientes";
        }


        model.addAttribute(
            "paciente",
            paciente
        );


        return "FormularioPaciente";
    }


    /*
     * ============================================================
     * GUARDAR PACIENTE
     * ============================================================
     */
    @PostMapping("/GuardarPaciente")
    public String updatePaciente(
        @ModelAttribute Paciente paciente,
        Model model,
        RedirectAttributes redirectAttributes
    ) {

        String mensaje =
            service.GuardarPaciente(
                paciente
            );


        if (
            mensaje.equals(
                "El paciente ha sido guardado correctamente"
            )
        ) {

            redirectAttributes.addFlashAttribute(
                "success",
                mensaje
            );


            return "redirect:/listaPacientes";

        } else {

            model.addAttribute(
                "error",
                mensaje
            );


            model.addAttribute(
                "paciente",
                paciente
            );


            return "FormularioPaciente";
        }
    }


    /*
     * ============================================================
     * ELIMINAR PACIENTE
     * ============================================================
     */
    @GetMapping({
        "/eliminarPaciente",
        "/EliminarPaciente"
    })
    public String deletePaciente(
        @RequestParam Long id,
        RedirectAttributes redirectAttributes
    ) {

        service.EliminarPaciente(
            id
        );


        redirectAttributes.addFlashAttribute(
            "success",
            "Paciente eliminado correctamente"
        );


        return "redirect:/listaPacientes";
    }
}