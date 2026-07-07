package com.Proyecto.Hospital.controller;

import com.Proyecto.Hospital.Model.Usuario;
import com.Proyecto.Hospital.Repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PerfilController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public PerfilController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/perfil")
    public String perfil(Authentication authentication, Model model) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));
        model.addAttribute("usuario", usuario);
        return "perfil";
    }

    @PostMapping("/actualizarPerfil")
    public String actualizarPerfil(@ModelAttribute Usuario usuarioForm,
                                   @RequestParam(required = false) String password,
                                   Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));

        usuario.setNombre(usuarioForm.getNombre());
        usuario.setEmail(usuarioForm.getEmail());

        if (password != null && !password.isBlank()) {
            usuario.setPassword(passwordEncoder.encode(password));
        }

        usuarioRepository.save(usuario);
        return "redirect:/inicio?perfilActualizado";
    }
}