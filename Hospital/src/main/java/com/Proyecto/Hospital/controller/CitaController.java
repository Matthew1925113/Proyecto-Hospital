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
    @GetMapping("/citas")
    public String listarCitas(Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }

        if ("ADMIN".equalsIgnoreCase(usuario.getRol())) {
            model.addAttribute("citas", citaService.listarTodos());
        } else {
            model.addAttribute("citas", citaService.ListarPorUsuario(usuario.getId()));
        }
        model.addAttribute("usuario", usuario);
        return "citas";
    }

    @GetMapping("/citas/nueva")
    public String nuevaCita(Model model){
        model.addAttribute("medicos", medicoService.ListarMedicos());
        return "formularioCita";
    }

    @PostMapping("/citas/reservar")
    public String reservarCita(@RequestParam Long medicoId,
                                @RequestParam String fechaStr,
                                @RequestParam String horaStr,
                                Model model, Principal principal) {

        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }
        String mensaje = citaService.Reservar(usuario, medicoId, LocalDate.parse(fechaStr), LocalTime.parse(horaStr));

        if(mensaje.equals("Cita Reservada")) {
            return "redirect:/citas";
        } else {
            model.addAttribute("error", mensaje);
            model.addAttribute("medicos", medicoService.ListarMedicos());
            return "formularioCita";

        }

    }

    @GetMapping("/citas/cancelar/{id}")
    public String cancelarCita(@PathVariable Long id, Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }
        String mensaje = citaService.Cancelar(id, usuario);
        if(mensaje.equals("Cita Cancelada")) {
            return "redirect:/citas";
        } else {
            model.addAttribute("error", mensaje);
            return "formularioCita";
        }
    }

    @GetMapping("/citas/confirmar/{id}")
    public String confirmarCita(@PathVariable Long id, Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }
        String mensaje = citaService.Confirmar(id, usuario);
        if(mensaje.equals("Cita Confirmada")) {
            return "redirect:/citas";
        } else {
            model.addAttribute("error", mensaje);
            return "formularioCita";
        }
    }

}

