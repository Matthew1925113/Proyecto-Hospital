package com.Proyecto.Hospital.controller;

import com.Proyecto.Hospital.Model.Medico;
import com.Proyecto.Hospital.Service.DisponibilidadMedicaService;
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

    private final DisponibilidadMedicaService disponibilidadService;


    public MedicoController(
        MedicoService service,
        DisponibilidadMedicaService disponibilidadService
    ) {

        this.service =
            service;

        this.disponibilidadService =
            disponibilidadService;
    }


    /*
     * ============================================================
     * LISTAR MÉDICOS
     * ============================================================
     */
    @GetMapping("/listaMedicos")
    public String listaMedicos(
        Model model
    ) {

        model.addAttribute(
            "medicos",
            service.ListarMedicos()
        );


        return "listaMedicos";
    }


    /*
     * ============================================================
     * NUEVO MÉDICO
     * ============================================================
     */
    @GetMapping({
        "/nuevoMedico",
        "/NuevoMedico"
    })
    public String nuevoMedicoForm(
        Model model
    ) {

        model.addAttribute(
            "medico",
            new Medico()
        );


        /*
         * Como todavía no existe el médico,
         * no hay disponibilidades que cargar.
         */
        model.addAttribute(
            "disponibilidades",
            java.util.List.of()
        );


        return "formularioMedico";
    }


    /*
     * ============================================================
     * GUARDAR MÉDICO
     * ============================================================
     */
    @PostMapping({
        "/guardarMedico",
        "/GuardarMedico"
    })
    public String guardarMedico(
        Medico medico,
        Model model
    ) {

        String mensaje =
            service.GuardarMedico(
                medico
            );


        /*
         * Si se guardó correctamente,
         * regresamos a la lista.
         */
        if (
            "El medico ha sido guardado correctamente"
                .equals(mensaje)
        ) {

            return "redirect:/listaMedicos";
        }


        /*
         * Si ocurrió un error,
         * regresamos al mismo formulario.
         */
        model.addAttribute(
            "error",
            mensaje
        );


        model.addAttribute(
            "medico",
            medico
        );


        /*
         * Si estamos editando un médico existente,
         * necesitamos volver a cargar sus disponibilidades
         * para que la vista no pierda esa información.
         */
        if (
            medico.getId() != null
        ) {

            model.addAttribute(
                "disponibilidades",
                disponibilidadService.listarPorMedico(
                    medico.getId()
                )
            );

        } else {

            model.addAttribute(
                "disponibilidades",
                java.util.List.of()
            );
        }


        return "formularioMedico";
    }


    /*
     * ============================================================
     * EDITAR MÉDICO
     * ============================================================
     */
    @GetMapping({
        "/editarMedico",
        "/EditarMedico"
    })
    public String editarMedicoForm(
        @RequestParam Long id,
        Model model
    ) {

        Medico medico =
            service.ObtenerMedico(
                id
            );


        /*
         * Si no existe,
         * regresamos a la lista.
         */
        if (
            medico == null
        ) {

            return "redirect:/listaMedicos";
        }


        /*
         * Datos principales del médico.
         */
        model.addAttribute(
            "medico",
            medico
        );


        /*
         * ========================================================
         * DISPONIBILIDADES DEL MÉDICO
         * ========================================================
         *
         * Este es el cambio importante.
         *
         * Ahora formularioMedico.html recibirá:
         *
         * ${disponibilidades}
         *
         * con todos los espacios activos de ese médico.
         */
        model.addAttribute(
            "disponibilidades",
            disponibilidadService.listarPorMedico(
                medico.getId()
            )
        );


        return "formularioMedico";
    }


    /*
     * ============================================================
     * ELIMINAR MÉDICO
     * ============================================================
     */
    @GetMapping({
        "/eliminarMedico",
        "/EliminarMedico"
    })
    public String deleteMedico(
        @RequestParam Long id,
        Model model
    ) {

        String mensaje =
            service.EliminarMedico(
                id
            );


        if (
            "El medico ha sido eliminado correctamente"
                .equals(mensaje)
        ) {

            return "redirect:/listaMedicos";
        }



        model.addAttribute(
            "error",
            mensaje
        );


        model.addAttribute(
            "medicos",
            service.ListarMedicos()
        );


        return "listaMedicos";
    }


    /*
     * ============================================================
     * API DE MÉDICOS
     * ============================================================
     */
    @GetMapping("/api/medicos")
    @ResponseBody
    public Iterable<Medico> listarMedicosJson() {

        return service.ListarMedicos();
    }
}