package com.Proyecto.Hospital.Service;

import com.Proyecto.Hospital.Model.Usuario;
import com.Proyecto.Hospital.Repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> ListarUsuarios() {
        return repository.findAll();
    }

    public Usuario ObtenerUsuario(Long id) {
        return repository.findById(id).orElse(null);
    }

    public String GuardarUsuario(Usuario usuario) {
        // Validar duplicados de email
        if (usuario.getId() == null) {
            if (repository.findByEmail(usuario.getEmail()).isPresent()) {
                return "El email ya está en uso";
            }
        } else {
            Usuario existente = repository.findByEmail(usuario.getEmail()).orElse(null);
            if (existente != null && !existente.getId().equals(usuario.getId())) {
                return "El email ya está en uso";
            }
        }

        // Manejar contraseña
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()
                && (usuario.getId() == null || !usuario.getPassword().startsWith("$2a$"))) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        } else if (usuario.getId() != null) {
            Usuario existente = ObtenerUsuario(usuario.getId());
            if (existente != null) {
                usuario.setPassword(existente.getPassword());
            }
        }

        repository.save(usuario);
        return "El usuario ha sido guardado correctamente";
    }

    public void EliminarUsuario(Long id) {
        repository.deleteById(id);
    }
}
