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
        model.addAttribute("citas", citaService.ListarPorUsuario(usuario.getId()));
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
}

