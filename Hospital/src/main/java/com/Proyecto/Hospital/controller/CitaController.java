package com.Proyecto.Hospital.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.Proyecto.Hospital.Service.CitaService;
import com.Proyecto.Hospital.Service.MedicoService;
import com.Proyecto.Hospital.Repository.UsuarioRepository;
import com.Proyecto.Hospital.Model.Usuario;
import org.springframework.ui.Model;
import java.security.Principal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.Proyecto.Hospital.Model.Cita;

@Controller
public class CitaController {

    private final CitaService citaService;
    private final MedicoService medicoService;
    private final UsuarioRepository usuarioRepository;

    public CitaController(CitaService citaService, MedicoService medicoService, UsuarioRepository usuarioRepository) {
        this.citaService = citaService;
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
     * Reservar una cita
     * Parámetros: medicoId, fechaStr (YYYY-MM-DD), horaStr (HH:mm)
     */
    @PostMapping("/citas/reservar")
    public String reservarCita(@RequestParam Long medicoId,
                              @RequestParam String fechaStr,
                              @RequestParam String horaStr,
                              Model model, Principal principal) {

        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }
        
        try {
            LocalDate fecha = LocalDate.parse(fechaStr);
            LocalTime hora = LocalTime.parse(horaStr);
            String mensaje = citaService.Reservar(usuario, medicoId, fecha, hora);

            if (mensaje.equals("Cita Reservada")) {
                return "redirect:/citas";
            } else {
                model.addAttribute("error", mensaje);
                model.addAttribute("medicos", medicoService.ListarMedicos());
                return "formularioCita";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar la cita: " + e.getMessage());
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
        
        String motivoCancelacion = (motivo != null && !motivo.isEmpty()) ? motivo : "Cancelado por el usuario";
        String mensaje = citaService.Cancelar(id, usuario);
        
        if (mensaje.equals("Cita Cancelada")) {
            return "redirect:/citas";
        } else {
            model.addAttribute("error", mensaje);
            model.addAttribute("citas", citaService.ListarPorUsuario(usuario.getId()));
            return "citas";
        }
    }

    /**
     * Confirmar una cita (cambiar de PENDIENTE a CONFIRMADA)
     * Solo admin puede hacer esto
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
            model.addAttribute("citas", citaService.listarTodos());
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
        
        java.util.Optional<Cita> citaOpt = java.util.Optional.ofNullable(
            citaService.ListarPorUsuario(usuario.getId()).stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null)
        );
        
        // Admin puede ver cualquier cita
        if ("ADMIN".equalsIgnoreCase(usuario.getRol())) {
            citaOpt = citaService.listarTodos().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
        }
        
        if (citaOpt.isEmpty()) {
            return "redirect:/citas";
        }
        
        model.addAttribute("cita", citaOpt.get());
        model.addAttribute("usuario", usuario);
        return "detalleCita";
    }
}
