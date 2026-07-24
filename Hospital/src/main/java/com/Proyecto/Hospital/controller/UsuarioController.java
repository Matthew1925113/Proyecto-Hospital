package com.Proyecto.Hospital.controller;

import com.Proyecto.Hospital.Model.Usuario;
import com.Proyecto.Hospital.Service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping({"/listaUsuarios", "/Usuarios"})
    public String index(Model model) {
        model.addAttribute("listaUsuarios", service.ListarUsuarios());
        return "Usuarios";
    }

    @GetMapping({"/nuevoUsuario", "/FormularioUsuario"})
    public String newUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "FormularioUsuario";
    }

    @GetMapping({"/EditarUsuario/{id}", "/editarUsuario/{id}"})
    public String editUsuario(@PathVariable Long id, Model model) {
        Usuario usuario = service.ObtenerUsuario(id);
        if (usuario == null) {
            return "redirect:/listaUsuarios";
        }
        model.addAttribute("usuario", usuario);
        return "FormularioUsuario";
    }

    @PostMapping("/GuardarUsuario")
    public String updateUsuario(@ModelAttribute Usuario usuario , Model model) {
        String mensaje = service.GuardarUsuario(usuario);
        if (mensaje.equals("El usuario ha sido guardado correctamente")) {
            return "redirect:/listaUsuarios";
        } else {
            model.addAttribute("error", mensaje);
            model.addAttribute("usuario", usuario);
            return "FormularioUsuario";
        }
    }

    @GetMapping({"/eliminarUsuario", "/EliminarUsuario"})
    public String deleteUsuario(@RequestParam Long id) {
        service.EliminarUsuario(id);
        return "redirect:/listaUsuarios";
    }
}
