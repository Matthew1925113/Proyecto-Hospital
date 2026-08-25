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
public class SecurityConfig {

    private final LoginSecurityHeadler loginSecurityHeadler;

    public SecurityConfig(
        LoginSecurityHeadler loginSecurityHeadler
    ) {
        this.loginSecurityHeadler = loginSecurityHeadler;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(
        HttpSecurity http
    ) throws Exception {

        http

            .authorizeHttpRequests(auth -> auth


                /*
                 * =================================================
                 * RUTAS PÚBLICAS
                 * =================================================
                 */
                .requestMatchers(
                    "/login",
                    "/acceso-denegado",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**"
                )
                .permitAll()


                /*
                 * =================================================
                 * INICIO
                 * =================================================
                 */
                .requestMatchers(
                    "/inicio"
                )
                .hasAnyRole(
                    "ADMIN",
                    "USUARIO",
                    "MEDICO"
                )


                /*
                 * =================================================
                 * GESTIÓN DE PACIENTES
                 * SOLO ADMIN
                 * =================================================
                 */
                .requestMatchers(
                    "/listaPacientes",
                    "/pacienteNuevo",
                    "/FormularioPaciente",

                    "/EditarPaciente/**",
                    "/editarPaciente/**",

                    "/GuardarPaciente",

                    "/eliminarPaciente",
                    "/EliminarPaciente",

                    "/telefono/**"
                )
                .hasRole("ADMIN")


                /*
                 * =================================================
                 * GESTIÓN DE MÉDICOS
                 * SOLO ADMIN
                 * =================================================
                 *
                 * Estas son las rutas REALES que utiliza
                 * MedicoController.
                 */
                .requestMatchers(
                    "/listaMedicos",

                    "/nuevoMedico",
                    "/NuevoMedico",

                    "/guardarMedico",
                    "/GuardarMedico",

                    "/editarMedico",
                    "/EditarMedico",

                    "/eliminarMedico",
                    "/EliminarMedico",

                    "/disponibilidades/**"
                )
                .hasRole("ADMIN")


                /*
                 * =================================================
                 * GESTIÓN DE USUARIOS
                 * SOLO ADMIN
                 * =================================================
                 */
                .requestMatchers(
                    "/listaUsuarios",
                    "/Usuarios",

                    "/nuevoUsuario",
                    "/FormularioUsuario",

                    "/EditarUsuario/**",
                    "/editarUsuario/**",

                    "/GuardarUsuario",

                    "/eliminarUsuario",
                    "/EliminarUsuario"
                )
                .hasRole("ADMIN")


                /*
                 * =================================================
                 * SOLICITAR / RESERVAR CITAS
                 * =================================================
                 *
                 * El paciente puede solicitar.
                 * El administrador también.
                 *
                 * El médico NO solicita citas desde aquí.
                 */
                .requestMatchers(
                    "/citas/nueva",
                    "/citas/reservar",
                    "/citas/horarios-disponibles"
                )
                .hasAnyRole(
                    "ADMIN",
                    "USUARIO"
                )


                /*
                 * =================================================
                 * CONFIRMAR / RECHAZAR
                 * =================================================
                 *
                 * MEDICO:
                 * puede gestionar solamente sus propias citas.
                 *
                 * ADMIN:
                 * puede gestionar cualquiera.
                 *
                 * La comprobación de propiedad también
                 * se realiza dentro de CitaService.
                 */
                .requestMatchers(
                    "/citas/confirmar/**",
                    "/citas/rechazar/**"
                )
                .hasAnyRole(
                    "ADMIN",
                    "MEDICO"
                )


                /*
                 * =================================================
                 * CANCELAR CITA
                 * =================================================
                 *
                 * Paciente -> CANCELAR
                 * Médico   -> RECHAZAR
                 */
                .requestMatchers(
                    "/citas/cancelar/**"
                )
                .hasAnyRole(
                    "ADMIN",
                    "USUARIO"
                )


                /*
                 * =================================================
                 * CONSULTAR CITAS
                 * =================================================
                 *
                 * Los tres pueden entrar.
                 *
                 * CitaController decide cuáles puede
                 * ver cada uno.
                 */
                .requestMatchers(
                    "/citas/**"
                )
                .hasAnyRole(
                    "ADMIN",
                    "USUARIO",
                    "MEDICO"
                )


                /*
                 * =================================================
                 * PERFIL DEL PACIENTE
                 * =================================================
                 */
                .requestMatchers(
                    "/perfil",
                    "/actualizarPerfil"
                )
                .hasAnyRole(
                    "ADMIN",
                    "USUARIO"
                )


                /*
                 * API existente de médicos.
                 *
                 * Requiere como mínimo haber iniciado sesión.
                 */
                .requestMatchers(
                    "/api/medicos"
                )
                .authenticated()


                /*
                 * Cualquier otra ruta:
                 * debe tener sesión iniciada.
                 */
                .anyRequest()
                .authenticated()
            )


            /*
             * =====================================================
             * LOGIN
             * =====================================================
             */
            .formLogin(form -> form

                .loginPage(
                    "/login"
                )

                .usernameParameter(
                    "email"
                )

                .successHandler(
                    loginSecurityHeadler
                )

                .permitAll()
            )


            /*
             * =====================================================
             * LOGOUT
             * =====================================================
             */
            .logout(logout -> logout

                .logoutSuccessUrl(
                    "/login?logout"
                )

                .permitAll()
            )


            /*
             * =====================================================
             * ACCESO DENEGADO
             * =====================================================
             */
            .exceptionHandling(ex -> ex

                .accessDeniedPage(
                    "/acceso-denegado"
                )
            )


            /*
             * =====================================================
             * H2
             * =====================================================
             */
            .headers(headers -> headers

                .frameOptions(
                    frame -> frame.sameOrigin()
                )
            )


            .csrf(csrf -> csrf

                .ignoringRequestMatchers(
                    "/h2-console/**"
                )
            );


        return http.build();
    }
}