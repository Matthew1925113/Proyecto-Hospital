package com.Proyecto.Hospital.Config;

import com.Proyecto.Hospital.Security.LoginSecurityHeadler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                .requestMatchers("/inicio").hasAnyRole("ADMIN", "USUARIO")
                .requestMatchers("/listaPacientes", "/pacienteNuevo", "/FormularioPaciente", "/EditarPaciente/**", "/editarPaciente/**", "/GuardarPaciente", "/eliminarPaciente", "/EliminarPaciente", "/medicos").hasRole("ADMIN")
                .requestMatchers("/citas", "/perfil", "/actualizarPerfil").hasRole("USUARIO")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .successHandler(loginSecurityHeadler)
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
