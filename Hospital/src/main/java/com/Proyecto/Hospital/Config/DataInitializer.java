package com.Proyecto.Hospital.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.Proyecto.Hospital.Model.Usuario;
import com.Proyecto.Hospital.Repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if(usuarioRepository.findByEmail("Juan@hospital.com").isEmpty()){
            Usuario admin = new Usuario();
            admin.setNombre("Juan");
            admin.setEmail("Juan@hospital.com");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRol("ADMIN");
            usuarioRepository.save(admin);
        }

        if(usuarioRepository.findByEmail("Pedro@hospital.com").isEmpty()){
            Usuario usuario = new Usuario();
            usuario.setNombre("Pedro");
            usuario.setEmail("Pedro@hospital.com");
            usuario.setPassword(passwordEncoder.encode("123456"));
            usuario.setRol("USUARIO");
            usuarioRepository.save(usuario);
        }
    }
}
