package com.Proyecto.Hospital.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import com.Proyecto.Hospital.Service.DisponibilidadMedicaService;
import com.Proyecto.Hospital.Service.MedicoService;
import com.Proyecto.Hospital.Repository.UsuarioRepository;
import com.Proyecto.Hospital.Model.Usuario;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.Proyecto.Hospital.Model.DisponibilidadMedica;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class DisponibilidadController {

    private static final Logger logger = LoggerFactory.getLogger(DisponibilidadController.class);
    
    private final DisponibilidadMedicaService disponibilidadService;
    private final MedicoService medicoService;
    private final UsuarioRepository usuarioRepository;

    public DisponibilidadController(DisponibilidadMedicaService disponibilidadService,
                                   MedicoService medicoService,
                                   UsuarioRepository usuarioRepository) {
        this.disponibilidadService = disponibilidadService;
        this.medicoService = medicoService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/disponibilidades/editar")
    public String editarDisponibilidades(@RequestParam(required = false) Long medicoId,
                                         @RequestParam(required = false) String fecha,
                                         Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuario == null || !"ADMIN".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/login";
        }

        model.addAttribute("medicos", medicoService.ListarMedicos());
        
        if (medicoId != null && fecha != null) {
            try {
                LocalDate fechaObj = LocalDate.parse(fecha);
                List<DisponibilidadMedica> disponibilidades = 
                    disponibilidadService.listarPorMedicoYFecha(medicoId, fechaObj);
                model.addAttribute("disponibilidades", disponibilidades);
                model.addAttribute("medicoSeleccionado", medicoId);
                model.addAttribute("fechaSeleccionada", fecha);
            } catch (Exception e) {
                logger.error("Error al cargar disponibilidades", e);
                model.addAttribute("error", "Fecha inválida");
            }
        }
        
        return "editarDisponibilidad";
    }

    @PostMapping("/disponibilidades/actualizar/{id}")
    public String actualizarDisponibilidad(@PathVariable Long id,
                                          @RequestParam LocalTime horaInicio,
                                          @RequestParam LocalTime horaFin,
                                          @RequestParam Boolean activo,
                                          Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuario == null || !"ADMIN".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/login";
        }

        try {
            if (!horaInicio.isBefore(horaFin)) {
                model.addAttribute("error", "Hora inicio debe ser menor que hora fin");
                model.addAttribute("medicos", medicoService.ListarMedicos());
                return "editarDisponibilidad";
            }

            disponibilidadService.actualizar(id, horaInicio, horaFin, activo);
            logger.info("Disponibilidad {} actualizada por admin {}", id, usuario.getEmail());
            
            return "redirect:/disponibilidades/editar";
        } catch (Exception e) {
            logger.error("Error al actualizar disponibilidad", e);
            model.addAttribute("error", "Error: " + e.getMessage());
            model.addAttribute("medicos", medicoService.ListarMedicos());
            return "editarDisponibilidad";
        }
    }

    @PostMapping("/disponibilidades/actualizar-rango")
    public String actualizarDisponibilidadRango(@RequestParam Long medicoId,
                                               @RequestParam String fechaDesde,
                                               @RequestParam String fechaHasta,
                                               @RequestParam LocalTime horaInicio,
                                               @RequestParam LocalTime horaFin,
                                               @RequestParam Boolean activo,
                                               Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuario == null || !"ADMIN".equalsIgnoreCase(usuario.getRol())) {
            return "redirect:/login";
        }

        try {
            if (!horaInicio.isBefore(horaFin)) {
                model.addAttribute("error", "Hora inicio debe ser menor que hora fin");
                model.addAttribute("medicos", medicoService.ListarMedicos());
                return "editarDisponibilidad";
            }

            LocalDate desde = LocalDate.parse(fechaDesde);
            LocalDate hasta = LocalDate.parse(fechaHasta);

            if (desde.isAfter(hasta)) {
                model.addAttribute("error", "Fecha desde debe ser menor que fecha hasta");
                model.addAttribute("medicos", medicoService.ListarMedicos());
                return "editarDisponibilidad";
            }

            disponibilidadService.actualizarRango(medicoId, desde, hasta, horaInicio, horaFin, activo);
            logger.info("Disponibilidades en rango actualizado por admin {}", usuario.getEmail());
            
            return "redirect:/disponibilidades/editar";
        } catch (Exception e) {
            logger.error("Error al actualizar disponibilidades en rango", e);
            model.addAttribute("error", "Error: " + e.getMessage());
            model.addAttribute("medicos", medicoService.ListarMedicos());
            return "editarDisponibilidad";
        }
    }
}
