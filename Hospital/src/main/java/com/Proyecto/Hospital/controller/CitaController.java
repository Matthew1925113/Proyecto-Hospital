package com.Proyecto.Hospital.controller;

import com.Proyecto.Hospital.Model.Cita;
import com.Proyecto.Hospital.Model.DisponibilidadMedica;
import com.Proyecto.Hospital.Model.Medico;
import com.Proyecto.Hospital.Model.Usuario;

import com.Proyecto.Hospital.Repository.DisponibilidadMedicaRepository;
import com.Proyecto.Hospital.Repository.UsuarioRepository;

import com.Proyecto.Hospital.Service.CitaService;
import com.Proyecto.Hospital.Service.MedicoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class CitaController {

    private static final Logger logger =
        LoggerFactory.getLogger(CitaController.class);

    private static final DateTimeFormatter FORMATO_HORA =
        DateTimeFormatter.ofPattern("HH:mm");


    private final CitaService citaService;

    private final MedicoService medicoService;

    private final DisponibilidadMedicaRepository disponibilidadRepository;

    private final UsuarioRepository usuarioRepository;


    public CitaController(
        CitaService citaService,
        MedicoService medicoService,
        DisponibilidadMedicaRepository disponibilidadRepository,
        UsuarioRepository usuarioRepository
    ) {

        this.citaService =
            citaService;

        this.medicoService =
            medicoService;

        this.disponibilidadRepository =
            disponibilidadRepository;

        this.usuarioRepository =
            usuarioRepository;
    }


    /*
     * ============================================================
     * LISTAR CITAS
     * ============================================================
     *
     * ADMIN:
     * ve todas las citas.
     *
     * USUARIO:
     * ve únicamente sus propias citas.
     *
     * MEDICO:
     * ve únicamente las citas asignadas a él.
     */
    @GetMapping("/citas")
    public String listarCitas(
        @RequestParam(required = false) String estado,
        @RequestParam(required = false) Long medicoId,
        @RequestParam(required = false) String especialidad,
        @RequestParam(required = false) String fechaDesdeStr,
        @RequestParam(required = false) String fechaHastaStr,
        Model model,
        Principal principal
    ) {

        try {

            Usuario usuario =
                obtenerUsuarioAutenticado(
                    principal
                );


            if (usuario == null) {

                return "redirect:/login";
            }


            /*
             * ====================================================
             * ADMIN
             * ====================================================
             */
            if (
                "ADMIN".equalsIgnoreCase(
                    usuario.getRol()
                )
            ) {

                LocalDate desde =
                    (
                        fechaDesdeStr == null
                        || fechaDesdeStr.isBlank()
                    )
                        ? null
                        : LocalDate.parse(
                            fechaDesdeStr
                        );


                LocalDate hasta =
                    (
                        fechaHastaStr == null
                        || fechaHastaStr.isBlank()
                    )
                        ? null
                        : LocalDate.parse(
                            fechaHastaStr
                        );


                model.addAttribute(
                    "citas",
                    citaService.Filtrar(
                        estado,
                        medicoId,
                        especialidad,
                        desde,
                        hasta
                    )
                );


                model.addAttribute(
                    "medicos",
                    medicoService.ListarMedicos()
                );
            }


            /*
             * ====================================================
             * MEDICO
             * ====================================================
             */
            else if (
                "MEDICO".equalsIgnoreCase(
                    usuario.getRol()
                )
            ) {

                /*
                 * Buscamos qué médico corresponde
                 * al usuario autenticado.
                 *
                 * La relación se realiza utilizando
                 * el mismo correo electrónico.
                 */
                Medico medico =
                    medicoService.ObtenerPorEmail(
                        usuario.getEmail()
                    );


                /*
                 * Si existe una cuenta con rol MEDICO
                 * pero no existe un registro médico con
                 * el mismo correo, mostramos el problema.
                 */
                if (medico == null) {

                    model.addAttribute(
                        "error",
                        "Tu cuenta tiene rol MEDICO, pero no está vinculada "
                        + "a ningún médico. El correo de la cuenta debe coincidir "
                        + "con el correo registrado del médico."
                    );


                    model.addAttribute(
                        "citas",
                        List.of()
                    );

                } else {

                    /*
                     * Guardamos el médico actual para utilizarlo
                     * posteriormente en la vista.
                     */
                    model.addAttribute(
                        "medicoActual",
                        medico
                    );


                    /*
                     * El médico únicamente ve sus citas.
                     */
                    model.addAttribute(
                        "citas",
                        citaService.ListarPorMedico(
                            medico.getId()
                        )
                    );
                }
            }


            /*
             * ====================================================
             * PACIENTE / USUARIO
             * ====================================================
             */
            else {

                model.addAttribute(
                    "citas",
                    citaService.ListarPorUsuario(
                        usuario.getId()
                    )
                );
            }


            model.addAttribute(
                "usuario",
                usuario
            );


            return "citas";


        } catch (Exception e) {

            logger.error(
                "Error al listar citas",
                e
            );


            model.addAttribute(
                "error",
                "Error al cargar las citas: "
                    + e.getMessage()
            );


            return "error";
        }
    }


    /*
     * ============================================================
     * FORMULARIO PARA SOLICITAR CITA
     * ============================================================
     *
     * El formulario está pensado para pacientes.
     *
     * Una cuenta MEDICO no debe solicitar citas
     * desde esta sección.
     */
    @GetMapping("/citas/nueva")
    public String nuevaCita(
        Model model,
        Principal principal
    ) {

        try {

            Usuario usuario =
                obtenerUsuarioAutenticado(
                    principal
                );


            if (usuario == null) {

                return "redirect:/login";
            }


            /*
             * El médico no necesita utilizar
             * el formulario de solicitud de citas.
             */
            if (
                "MEDICO".equalsIgnoreCase(
                    usuario.getRol()
                )
            ) {

                return "redirect:/citas";
            }


            logger.info(
                "Usuario {} accediendo al formulario de nueva cita",
                usuario.getEmail()
            );


            cargarDatosFormulario(
                model
            );


            model.addAttribute(
                "usuario",
                usuario
            );


            return "formularioCita";


        } catch (Exception e) {

            logger.error(
                "Error al cargar formulario de nueva cita",
                e
            );


            model.addAttribute(
                "error",
                "Error al cargar el formulario: "
                    + e.getMessage()
            );


            return "error";
        }
    }


    /*
     * ============================================================
     * OBTENER HORARIOS DISPONIBLES
     * ============================================================
     *
     * Genera bloques de una hora.
     *
     * Los horarios PENDIENTES y CONFIRMADOS
     * ya no aparecerán como disponibles.
     */
    @GetMapping("/citas/horarios-disponibles")
    @ResponseBody
    public List<Map<String, Object>> obtenerHorariosDisponibles(
        @RequestParam Long disponibilidadId
    ) {

        List<Map<String, Object>> horarios =
            new ArrayList<>();


        Optional<DisponibilidadMedica> disponibilidadOpt =
            disponibilidadRepository
                .findByIdAndActivoTrue(
                    disponibilidadId
                );


        if (
            disponibilidadOpt.isEmpty()
        ) {

            logger.warn(
                "Se solicitaron horarios de una disponibilidad inexistente o inactiva: {}",
                disponibilidadId
            );


            return horarios;
        }


        DisponibilidadMedica disponibilidad =
            disponibilidadOpt.get();


        /*
         * Validamos que la disponibilidad
         * tenga toda la información requerida.
         */
        if (
            disponibilidad.getMedico() == null
            || disponibilidad.getFecha() == null
            || disponibilidad.getHoraInicio() == null
            || disponibilidad.getHoraFin() == null
        ) {

            return horarios;
        }


        /*
         * No mostramos disponibilidades pasadas.
         */
        if (
            disponibilidad
                .getFecha()
                .isBefore(
                    LocalDate.now()
                )
        ) {

            return horarios;
        }


        LocalTime horaActual =
            disponibilidad.getHoraInicio();


        LocalTime horaFinal =
            disponibilidad.getHoraFin();


        /*
         * Ejemplo:
         *
         * disponibilidad:
         *
         * 08:00 - 12:00
         *
         * genera:
         *
         * 08:00 - 09:00
         * 09:00 - 10:00
         * 10:00 - 11:00
         * 11:00 - 12:00
         */
        while (
            horaActual
                .plusHours(1)
                .compareTo(
                    horaFinal
                ) <= 0
        ) {

            LocalTime inicioBloque =
                horaActual;


            LocalTime finBloque =
                horaActual.plusHours(1);


            boolean horaValida =
                true;


            /*
             * Si la disponibilidad corresponde
             * al día actual, ocultamos horas pasadas.
             */
            if (
                disponibilidad
                    .getFecha()
                    .equals(
                        LocalDate.now()
                    )
                &&
                !inicioBloque.isAfter(
                    LocalTime.now()
                )
            ) {

                horaValida =
                    false;
            }


            /*
             * Aquí se aplica la regla que ya hicimos:
             *
             * PENDIENTE = ocupado
             * CONFIRMADA = ocupado
             *
             * RECHAZADA = libre
             * CANCELADA = libre
             */
            boolean ocupado =
                citaService.estaHorarioOcupado(
                    disponibilidad
                        .getMedico()
                        .getId(),

                    disponibilidad.getFecha(),

                    inicioBloque
                );


            if (
                horaValida
                && !ocupado
            ) {

                Map<String, Object> horario =
                    new LinkedHashMap<>();


                horario.put(
                    "disponibilidadId",
                    disponibilidad.getId()
                );


                horario.put(
                    "horaInicio",
                    inicioBloque.format(
                        FORMATO_HORA
                    )
                );


                horario.put(
                    "horaFin",
                    finBloque.format(
                        FORMATO_HORA
                    )
                );


                horarios.add(
                    horario
                );
            }


            horaActual =
                horaActual.plusHours(1);
        }


        logger.debug(
            "Disponibilidad {} tiene {} bloques libres",
            disponibilidadId,
            horarios.size()
        );


        return horarios;
    }


    /*
     * ============================================================
     * RESERVAR CITA
     * ============================================================
     */
    @PostMapping("/citas/reservar")
    public String reservarCita(
        @RequestParam Long disponibilidadId,

        @RequestParam
        @DateTimeFormat(pattern = "HH:mm")
        LocalTime horaSeleccionada,

        Model model,

        Principal principal
    ) {

        Usuario usuario =
            obtenerUsuarioAutenticado(
                principal
            );


        if (usuario == null) {

            return "redirect:/login";
        }


        /*
         * Evitamos que una cuenta MEDICO
         * utilice directamente esta URL.
         */
        if (
            "MEDICO".equalsIgnoreCase(
                usuario.getRol()
            )
        ) {

            return "redirect:/citas";
        }


        try {

            logger.info(
                "Usuario {} intenta reservar disponibilidad {} a las {}",
                usuario.getEmail(),
                disponibilidadId,
                horaSeleccionada
            );


            String mensaje =
                citaService.Reservar(
                    usuario,
                    disponibilidadId,
                    horaSeleccionada
                );


            if (
                "Cita Reservada".equals(
                    mensaje
                )
            ) {

                return "redirect:/citas";
            }


            cargarDatosFormulario(
                model
            );


            model.addAttribute(
                "usuario",
                usuario
            );


            model.addAttribute(
                "error",
                mensaje
            );


            return "formularioCita";


        } catch (Exception e) {

            logger.error(
                "Excepción al reservar cita",
                e
            );


            cargarDatosFormulario(
                model
            );


            model.addAttribute(
                "usuario",
                usuario
            );


            model.addAttribute(
                "error",
                "Error al procesar la cita: "
                    + e.getMessage()
            );


            return "formularioCita";
        }
    }


    /*
     * ============================================================
     * CANCELAR CITA
     * ============================================================
     *
     * Esta acción será utilizada por:
     *
     * USUARIO
     * ADMIN
     *
     * El médico no utiliza CANCELAR para gestionar
     * solicitudes pendientes. El médico utiliza RECHAZAR.
     */
    @PostMapping("/citas/cancelar/{id}")
    public String cancelarCita(
        @PathVariable Long id,
        Model model,
        Principal principal
    ) {

        Usuario usuario =
            obtenerUsuarioAutenticado(
                principal
            );


        if (usuario == null) {

            return "redirect:/login";
        }


        /*
         * El médico no cancela solicitudes.
         */
        if (
            "MEDICO".equalsIgnoreCase(
                usuario.getRol()
            )
        ) {

            return "redirect:/citas";
        }


        try {

            logger.info(
                "Usuario {} intenta cancelar cita {}",
                usuario.getEmail(),
                id
            );


            String mensaje =
                citaService.Cancelar(
                    id,
                    usuario
                );


            if (
                "Cita Cancelada".equals(
                    mensaje
                )
            ) {

                return "redirect:/citas";
            }


            model.addAttribute(
                "error",
                mensaje
            );


            cargarCitasSegunRol(
                model,
                usuario
            );


            model.addAttribute(
                "usuario",
                usuario
            );


            return "citas";


        } catch (Exception e) {

            logger.error(
                "Excepción al cancelar cita",
                e
            );


            model.addAttribute(
                "error",
                "Error al cancelar la cita: "
                    + e.getMessage()
            );


            cargarCitasSegunRol(
                model,
                usuario
            );


            model.addAttribute(
                "usuario",
                usuario
            );


            return "citas";
        }
    }


    /*
     * ============================================================
     * CONFIRMAR CITA
     * ============================================================
     *
     * Puede utilizarlo:
     *
     * MEDICO -> solamente sus citas.
     *
     * ADMIN -> cualquier cita.
     */
    @PostMapping("/citas/confirmar/{id}")
    public String confirmarCita(
        @PathVariable Long id,
        Model model,
        Principal principal
    ) {

        Usuario usuario =
            obtenerUsuarioAutenticado(
                principal
            );


        if (usuario == null) {

            return "redirect:/login";
        }


        try {

            logger.info(
                "Usuario {} con rol {} intenta confirmar cita {}",
                usuario.getEmail(),
                usuario.getRol(),
                id
            );


            String mensaje =
                citaService.Confirmar(
                    id,
                    usuario
                );


            if (
                "Cita Confirmada".equals(
                    mensaje
                )
            ) {

                return "redirect:/citas";
            }


            model.addAttribute(
                "error",
                mensaje
            );


            cargarCitasSegunRol(
                model,
                usuario
            );


            model.addAttribute(
                "usuario",
                usuario
            );


            return "citas";


        } catch (Exception e) {

            logger.error(
                "Excepción al confirmar cita",
                e
            );


            model.addAttribute(
                "error",
                "Error al confirmar la cita: "
                    + e.getMessage()
            );


            cargarCitasSegunRol(
                model,
                usuario
            );


            model.addAttribute(
                "usuario",
                usuario
            );


            return "citas";
        }
    }


    /*
     * ============================================================
     * RECHAZAR CITA
     * ============================================================
     *
     * Puede utilizarlo:
     *
     * MEDICO -> solamente sus citas.
     *
     * ADMIN -> cualquier cita.
     *
     * Solamente una cita PENDIENTE puede rechazarse.
     *
     * Al quedar RECHAZADA, el horario vuelve
     * automáticamente a estar disponible.
     */
    @PostMapping("/citas/rechazar/{id}")
    public String rechazarCita(
        @PathVariable Long id,
        Model model,
        Principal principal
    ) {

        Usuario usuario =
            obtenerUsuarioAutenticado(
                principal
            );


        if (usuario == null) {

            return "redirect:/login";
        }


        try {

            logger.info(
                "Usuario {} con rol {} intenta rechazar cita {}",
                usuario.getEmail(),
                usuario.getRol(),
                id
            );


            String mensaje =
                citaService.Rechazar(
                    id,
                    usuario
                );


            if (
                "Cita Rechazada".equals(
                    mensaje
                )
            ) {

                return "redirect:/citas";
            }


            model.addAttribute(
                "error",
                mensaje
            );


            cargarCitasSegunRol(
                model,
                usuario
            );


            model.addAttribute(
                "usuario",
                usuario
            );


            return "citas";


        } catch (Exception e) {

            logger.error(
                "Excepción al rechazar cita",
                e
            );


            model.addAttribute(
                "error",
                "Error al rechazar la cita: "
                    + e.getMessage()
            );


            cargarCitasSegunRol(
                model,
                usuario
            );


            model.addAttribute(
                "usuario",
                usuario
            );


            return "citas";
        }
    }


    /*
     * ============================================================
     * VER DETALLE DE UNA CITA
     * ============================================================
     *
     * ADMIN:
     * puede ver cualquier cita.
     *
     * USUARIO:
     * solamente puede ver sus propias citas.
     *
     * MEDICO:
     * solamente puede ver las citas asignadas a él.
     */
    @GetMapping("/citas/{id}")
    public String verCita(
        @PathVariable Long id,
        Model model,
        Principal principal
    ) {

        Usuario usuario =
            obtenerUsuarioAutenticado(
                principal
            );


        if (usuario == null) {

            return "redirect:/login";
        }


        try {

            Optional<Cita> citaOpt =
                citaService.obtenerCitaPorId(
                    id
                );


            if (
                citaOpt.isEmpty()
            ) {

                return "redirect:/citas";
            }


            Cita cita =
                citaOpt.get();


            boolean esAdmin =
                "ADMIN".equalsIgnoreCase(
                    usuario.getRol()
                );


            /*
             * Comprobamos si el paciente autenticado
             * es el dueño de la cita.
             */
            boolean esPacientePropietario =
                "USUARIO".equalsIgnoreCase(
                    usuario.getRol()
                )
                &&
                cita.getUsuario() != null
                &&
                cita.getUsuario().getId() != null
                &&
                cita.getUsuario()
                    .getId()
                    .equals(
                        usuario.getId()
                    );


            /*
             * Comprobamos si el médico autenticado
             * es realmente el médico de la cita.
             */
            boolean esMedicoPropietario =
                "MEDICO".equalsIgnoreCase(
                    usuario.getRol()
                )
                &&
                cita.getMedico() != null
                &&
                cita.getMedico().getEmail() != null
                &&
                usuario.getEmail() != null
                &&
                cita.getMedico()
                    .getEmail()
                    .equalsIgnoreCase(
                        usuario.getEmail()
                    );


            /*
             * Si no es ADMIN, ni el paciente propietario,
             * ni el médico propietario, no puede acceder.
             */
            if (
                !esAdmin
                && !esPacientePropietario
                && !esMedicoPropietario
            ) {

                logger.warn(
                    "Usuario {} intentó acceder a una cita que no le pertenece",
                    usuario.getEmail()
                );


                return "redirect:/citas";
            }


            model.addAttribute(
                "cita",
                cita
            );


            model.addAttribute(
                "usuario",
                usuario
            );


            return "detalleCita";


        } catch (Exception e) {

            logger.error(
                "Error al ver detalles de cita",
                e
            );


            model.addAttribute(
                "error",
                "Error al cargar detalles de la cita: "
                    + e.getMessage()
            );


            return "error";
        }
    }


    /*
     * ============================================================
     * OBTENER USUARIO AUTENTICADO
     * ============================================================
     *
     * Evitamos repetir la búsqueda del usuario
     * en todos los métodos.
     */
    private Usuario obtenerUsuarioAutenticado(
        Principal principal
    ) {

        if (
            principal == null
        ) {

            return null;
        }


        return usuarioRepository
            .findByEmail(
                principal.getName()
            )
            .orElse(null);
    }


    /*
     * ============================================================
     * CARGAR CITAS SEGÚN EL ROL
     * ============================================================
     *
     * Se utiliza principalmente cuando ocurre
     * un error al confirmar, rechazar o cancelar.
     */
    private void cargarCitasSegunRol(
        Model model,
        Usuario usuario
    ) {

        /*
         * ADMIN
         */
        if (
            "ADMIN".equalsIgnoreCase(
                usuario.getRol()
            )
        ) {

            model.addAttribute(
                "citas",
                citaService.listarTodos()
            );


            model.addAttribute(
                "medicos",
                medicoService.ListarMedicos()
            );


            return;
        }


        /*
         * MEDICO
         */
        if (
            "MEDICO".equalsIgnoreCase(
                usuario.getRol()
            )
        ) {

            Medico medico =
                medicoService.ObtenerPorEmail(
                    usuario.getEmail()
                );


            if (
                medico == null
            ) {

                model.addAttribute(
                    "citas",
                    List.of()
                );


                model.addAttribute(
                    "error",
                    "La cuenta MEDICO no está vinculada "
                    + "a ningún médico registrado."
                );


                return;
            }


            model.addAttribute(
                "medicoActual",
                medico
            );


            model.addAttribute(
                "citas",
                citaService.ListarPorMedico(
                    medico.getId()
                )
            );


            return;
        }


        /*
         * PACIENTE
         */
        model.addAttribute(
            "citas",
            citaService.ListarPorUsuario(
                usuario.getId()
            )
        );
    }


    /*
     * ============================================================
     * RECARGAR DATOS DEL FORMULARIO
     * ============================================================
     */
    private void cargarDatosFormulario(
        Model model
    ) {

        model.addAttribute(
            "medicos",
            medicoService.ListarMedicos()
        );


        model.addAttribute(
            "disponibilidades",
            disponibilidadRepository
                .findAllActivas()
        );
    }
}