package com.Proyecto.Hospital.Config;

import com.Proyecto.Hospital.Security.LoginSecurityHeadler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig  {
    private final LoginSecurityHeadler loginSecurityHeadler;
    public SecurityConfig(LoginSecurityHeadler loginSecurityHeadler) {
        this.loginSecurityHeadler = loginSecurityHeadler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas
                .requestMatchers("/login", "/acceso-denegado", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                // Rutas privadas
                .requestMatchers("/perfil", "/actualizarPerfil").authenticated()  // Cualquier usuario autenticado
                .requestMatchers("/citas/**", "/inicio").hasAnyRole("ADMIN", "USUARIO")      // O solo ADMIN según necesidad
                .requestMatchers("/listaPacientes", "/pacienteNuevo", "/FormularioPaciente", 
                                "/EditarPaciente/**", "/editarPaciente/**", "/GuardarPaciente", 
                                "/eliminarPaciente", "/EliminarPaciente", 
                                "/medicos", "/listaMedicos", "/listaUsuarios", "/Usuarios", 
                                "/nuevoUsuario", "/FormularioUsuario", "/EditarUsuario/**", 
                                "/editarUsuario/**", "/GuardarUsuario", "/eliminarUsuario", 
                                "/EliminarUsuario" , "/citas/exportar/**").hasRole("ADMIN")  // Solo ADMIN
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .successHandler(loginSecurityHeadler)
                .failureHandler((request, response, exception) -> {
                    if (exception instanceof DisabledException) {
                        response.sendRedirect("/login?inactivo");
                    } else {
                        response.sendRedirect("/login?error");
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/acceso-denegado"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));

            return http.build();
    }
    
}