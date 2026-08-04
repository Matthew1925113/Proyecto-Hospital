package com.Proyecto.Hospital.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import com.Proyecto.Hospital.Service.CitaService;
import com.Proyecto.Hospital.Service.DisponibilidadMedicaService;
import com.Proyecto.Hospital.Service.MedicoService;
import com.Proyecto.Hospital.Repository.UsuarioRepository;
import com.Proyecto.Hospital.Model.Usuario;
import com.Proyecto.Hospital.Model.Cita;
import com.Proyecto.Hospital.Model.DisponibilidadMedica;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
public class CitaController {

    private final CitaService citaService;
    private final DisponibilidadMedicaService disponibilidadService;
    private final MedicoService medicoService;
    private final UsuarioRepository usuarioRepository;

    public CitaController(CitaService citaService, DisponibilidadMedicaService disponibilidadService,
                         MedicoService medicoService, UsuarioRepository usuarioRepository) {
        this.citaService = citaService;
        this.disponibilidadService = disponibilidadService;
        this.medicoService = medicoService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Listar citas
     * - Admin: Ve todas las citas con filtros
     * - Usuario: Ve solo sus propias citas
     */
    @GetMapping("/citas")
    public String listarCitas(@RequestParam(required = false) String estado,
                             @RequestParam(required = false) Long medicoId,
                             @RequestParam(required = false) String especialidad,
                             @RequestParam(required = false) String fechaDesdeStr,
                             @RequestParam(required = false) String fechaHastaStr,
                             Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }

        if ("ADMIN".equalsIgnoreCase(usuario.getRol())) {
            LocalDate desde = (fechaDesdeStr == null || fechaDesdeStr.isEmpty()) ? null : LocalDate.parse(fechaDesdeStr);
            LocalDate hasta = (fechaHastaStr == null || fechaHastaStr.isEmpty()) ? null : LocalDate.parse(fechaHastaStr);
            model.addAttribute("citas", citaService.Filtrar(estado, medicoId, especialidad, desde, hasta));
            model.addAttribute("medicos", medicoService.ListarMedicos());
        } else {
            model.addAttribute("citas", citaService.ListarPorUsuario(usuario.getId()));
        }
        model.addAttribute("usuario", usuario);
        return "citas";
    }

    /**
     * Página para crear nueva cita
     * Muestra los médicos disponibles
     */
    @GetMapping("/citas/nueva")
    public String nuevaCita(Model model) {
        model.addAttribute("medicos", medicoService.ListarMedicos());
        return "formularioCita";
    }

    /**
     * Obtener disponibilidades de un médico en una fecha
     * (Endpoint para cargar dinámicamente en el formulario)
     */
    @GetMapping("/api/disponibilidades")
    public String obtenerDisponibilidades(@RequestParam Long medicoId,
                                          @RequestParam String fechaStr,
                                          Model model) {
        LocalDate fecha = LocalDate.parse(fechaStr);
        List<DisponibilidadMedica> disponibilidades = disponibilidadService.listarPorMedicoYFecha(medicoId, fecha);
        model.addAttribute("disponibilidades", disponibilidades);
        return "disponibilidades";
    }

    /**
     * Reservar una cita
     * El usuario selecciona una disponibilidad (espacio médico + fecha + hora)
     */
    @PostMapping("/citas/reservar")
    public String reservarCita(@RequestParam Long disponibilidadId,
                               Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }

        String mensaje = citaService.Reservar(usuario, disponibilidadId);

        if (mensaje.equals("Cita Reservada")) {
            return "redirect:/citas";
        } else {
            model.addAttribute("error", mensaje);
            model.addAttribute("medicos", medicoService.ListarMedicos());
            return "formularioCita";
        }
    }

    /**
     * Cancelar una cita
     * - Usuario: Solo puede cancelar sus propias citas si aún no ocurrieron
     * - Admin: Puede cancelar cualquier cita en cualquier momento
     */
    @GetMapping("/citas/cancelar/{id}")
    public String cancelarCita(@PathVariable Long id,
                               @RequestParam(required = false) String motivo,
                               Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }
        
        String motivoCancelacion = motivo != null ? motivo : "Cancelado por el usuario";
        String mensaje = citaService.Cancelar(id, usuario, motivoCancelacion);
        
        if (mensaje.equals("Cita Cancelada")) {
            return "redirect:/citas";
        } else {
            model.addAttribute("error", mensaje);
            return "citas";
        }
    }

    /**
     * Confirmar una cita (cambiar de PENDIENTE a CONFIRMADA)
     * Solo admins pueden hacer esto
     */
    @PostMapping("/citas/confirmar/{id}")
    public String confirmarCita(@PathVariable Long id,
                                Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }
        
        String mensaje = citaService.Confirmar(id, usuario);
        
        if (mensaje.equals("Cita Confirmada")) {
            return "redirect:/citas";
        } else {
            model.addAttribute("error", mensaje);
            return "citas";
        }
    }

    /**
     * Ver detalles de una cita
     */
    @GetMapping("/citas/{id}")
    public String verCita(@PathVariable Long id,
                         Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }
        
        Optional<Cita> citaOpt = citaService.obtenerCitaPorId(id);
        if (citaOpt.isEmpty()) {
            return "redirect:/citas";
        }
        
        Cita cita = citaOpt.get();
        
        // Verificar permisos: el usuario solo puede ver sus citas, admin ve todas
        boolean esAdmin = "ADMIN".equalsIgnoreCase(usuario.getRol());
        if (!esAdmin && !cita.getUsuario().getId().equals(usuario.getId())) {
            return "redirect:/citas";
        }
        
        model.addAttribute("cita", cita);
        model.addAttribute("usuario", usuario);
        return "detalleCita";
    }
}
