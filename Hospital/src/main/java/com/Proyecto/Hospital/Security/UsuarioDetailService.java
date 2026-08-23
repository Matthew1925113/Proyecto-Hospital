package com.Proyecto.Hospital.Security;

import org.springframework.security.authentication.DisabledException;
import com.Proyecto.Hospital.Model.Usuario;
import com.Proyecto.Hospital.Repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado" + email));

        if ("INACTIVO".equals(usuario.getEstado())) {
            throw new DisabledException("Usuario inactivo");
        }
        return User.builder()
            .username(usuario.getEmail())
            .password(usuario.getPassword())
            .authorities(new SimpleGrantedAuthority("ROLE_" + usuario.getRol()))
            .build();
    }
    
}
