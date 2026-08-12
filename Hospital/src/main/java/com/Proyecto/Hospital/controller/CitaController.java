package com.Proyecto.Hospital.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.Proyecto.Hospital.Service.CitaService;
import com.Proyecto.Hospital.Service.MedicoService;
import com.Proyecto.Hospital.Service.DisponibilidadMedicaService;
import com.Proyecto.Hospital.Repository.UsuarioRepository;
import com.Proyecto.Hospital.Repository.DisponibilidadMedicaRepository;
import com.Proyecto.Hospital.Model.Usuario;
import com.Proyecto.Hospital.Model.Cita;
import org.springframework.ui.Model;
import java.security.Principal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import java.time.LocalDate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class CitaController {

    private static final Logger logger = LoggerFactory.getLogger(CitaController.class);
    
    private final CitaService citaService;
    private final MedicoService medicoService;
    private final DisponibilidadMedicaService disponibilidadService;
    private final DisponibilidadMedicaRepository disponibilidadRepository;
    private final UsuarioRepository usuarioRepository;

    public CitaController(CitaService citaService, MedicoService medicoService,
                         DisponibilidadMedicaService disponibilidadService,
                         DisponibilidadMedicaRepository disponibilidadRepository,
                         UsuarioRepository usuarioRepository) {
        this.citaService = citaService;
        this.medicoService = medicoService;
        this.disponibilidadService = disponibilidadService;
        this.disponibilidadRepository = disponibilidadRepository;
        this.usuarioRepository = usuarioRepository;
    }
    
    @GetMapping("/citas")
    public String listarCitas(@RequestParam(required = false) String estado,
                             @RequestParam(required = false) Long medicoId,
                             @RequestParam(required = false) String especialidad,
                             @RequestParam(required = false) String fechaDesdeStr,
                             @RequestParam(required = false) String fechaHastaStr,
                             Model model, Principal principal) {
        try {
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
        } catch (Exception e) {
            logger.error("Error al listar citas", e);
            model.addAttribute("error", "Error al cargar las citas: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/citas/nueva")
    public String nuevaCita(Model model) {
        try {
            logger.info("Accediendo a formulario de nueva cita");
            
            model.addAttribute("medicos", medicoService.ListarMedicos());
            // Cargar TODAS las disponibilidades activas
            model.addAttribute("disponibilidades", disponibilidadRepository.findAllActivas());
            
            logger.info("Disponibilidades cargadas: {}", disponibilidadRepository.findAllActivas().size());
            
            return "formularioCita";
        } catch (Exception e) {
            logger.error("Error al cargar formulario de nueva cita", e);
            model.addAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/citas/reservar")
    public String reservarCita(@RequestParam Long disponibilidadId,
                              Model model, Principal principal) {

        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuario == null) {
            logger.warn("Usuario no autenticado intentó reservar cita");
            return "redirect:/login";
        }
        
        try {
            logger.info("Usuario {} intenta reservar con disponibilidad {}", usuario.getEmail(), disponibilidadId);
            
            String mensaje = citaService.Reservar(usuario, disponibilidadId);

            logger.info("Resultado de reserva: {}", mensaje);
            
            if (mensaje.equals("Cita Reservada")) {
                logger.info("Cita reservada exitosamente para usuario: {}", usuario.getEmail());
                return "redirect:/citas";
            } else {
                logger.warn("Error al reservar cita: {}", mensaje);
                model.addAttribute("error", mensaje);
                model.addAttribute("medicos", medicoService.ListarMedicos());
                model.addAttribute("disponibilidades", disponibilidadRepository.findAllActivas());
                return "formularioCita";
            }
        } catch (Exception e) {
            logger.error("Excepción al reservar cita", e);
            model.addAttribute("error", "Error al procesar la cita: " + e.getMessage());
            model.addAttribute("medicos", medicoService.ListarMedicos());
            model.addAttribute("disponibilidades", disponibilidadRepository.findAllActivas());
            return "formularioCita";
        }
    }

    @GetMapping("/citas/cancelar/{id}")
    public String cancelarCita(@PathVariable Long id,
                              @RequestParam(required = false) String motivo,
                              Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }
        
        try {
            logger.info("Usuario {} intenta cancelar cita {}", usuario.getEmail(), id);
            String mensaje = citaService.Cancelar(id, usuario);
            
            if (mensaje.equals("Cita Cancelada")) {
                logger.info("Cita {} cancelada por usuario {}", id, usuario.getEmail());
                return "redirect:/citas";
            } else {
                logger.warn("Error al cancelar cita {}: {}", id, mensaje);
                model.addAttribute("error", mensaje);
                model.addAttribute("citas", citaService.ListarPorUsuario(usuario.getId()));
                return "citas";
            }
        } catch (Exception e) {
            logger.error("Excepción al cancelar cita", e);
            model.addAttribute("error", "Error al cancelar la cita: " + e.getMessage());
            return "citas";
        }
    }

    @GetMapping("/citas/confirmar/{id}")
    public String confirmarCita(@PathVariable Long id,
                               Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }
        
        try {
            logger.info("Admin {} intenta confirmar cita {}", usuario.getEmail(), id);
            String mensaje = citaService.Confirmar(id, usuario);
            
            if (mensaje.equals("Cita Confirmada")) {
                logger.info("Cita {} confirmada por admin {}", id, usuario.getEmail());
                return "redirect:/citas";
            } else {
                logger.warn("Error al confirmar cita {}: {}", id, mensaje);
                model.addAttribute("error", mensaje);
                model.addAttribute("citas", citaService.listarTodos());
                return "citas";
            }
        } catch (Exception e) {
            logger.error("Excepción al confirmar cita", e);
            model.addAttribute("error", "Error al confirmar la cita: " + e.getMessage());
            return "citas";
        }
    }

    @GetMapping("/citas/{id}")
    public String verCita(@PathVariable Long id,
                         Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }
        
        try {
            logger.info("Usuario {} intenta ver detalles de cita {}", usuario.getEmail(), id);
            
            Optional<Cita> citaOpt = citaService.obtenerCitaPorId(id);
            
            if (citaOpt.isEmpty()) {
                logger.warn("Cita {} no encontrada", id);
                return "redirect:/citas";
            }
            
            Cita cita = citaOpt.get();
            
            boolean esAdmin = "ADMIN".equalsIgnoreCase(usuario.getRol());
            if (!esAdmin && !cita.getUsuario().getId().equals(usuario.getId())) {
                logger.warn("Usuario {} intentó acceder a cita de otro usuario", usuario.getEmail());
                return "redirect:/citas";
            }
            
            model.addAttribute("cita", cita);
            model.addAttribute("usuario", usuario);
            logger.info("Detalles de cita {} cargados exitosamente", id);
            return "detalleCita";
        } catch (Exception e) {
            logger.error("Error al ver detalles de cita", e);
            model.addAttribute("error", "Error al cargar detalles de la cita: " + e.getMessage());
            return "error";
        }
    }
}