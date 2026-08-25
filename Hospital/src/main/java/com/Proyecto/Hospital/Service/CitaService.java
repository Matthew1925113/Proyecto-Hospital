package com.Proyecto.Hospital.Service;

import com.Proyecto.Hospital.Model.Cita;
import com.Proyecto.Hospital.Model.Medico;
import com.Proyecto.Hospital.Model.Usuario;
import com.Proyecto.Hospital.Model.DisponibilidadMedica;

import com.Proyecto.Hospital.Repository.CitaRepository;
import com.Proyecto.Hospital.Repository.DisponibilidadMedicaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class CitaService {

    private static final Logger logger =
        LoggerFactory.getLogger(CitaService.class);

    private static final int DURACION_CITA_HORAS = 1;

    /*
     * Estos son los únicos estados que ocupan
     * un horario del médico.
     *
     * PENDIENTE:
     * aunque todavía no haya sido aceptada,
     * el horario ya queda reservado.
     *
     * CONFIRMADA:
     * el horario continúa reservado.
     *
     * RECHAZADA y CANCELADA:
     * liberan el horario.
     */
    private static final Set<String> ESTADOS_QUE_OCUPAN_HORARIO =
        Set.of(
            "PENDIENTE",
            "CONFIRMADA"
        );


    private final CitaRepository citaRepository;

    private final DisponibilidadMedicaRepository disponibilidadRepository;

    private final NotificacionService notificacionService;


    public CitaService(
        CitaRepository citaRepository,
        DisponibilidadMedicaRepository disponibilidadRepository,
        NotificacionService notificacionService
    ) {

        this.citaRepository =
            citaRepository;

        this.disponibilidadRepository =
            disponibilidadRepository;

        this.notificacionService =
            notificacionService;
    }


    /*
     * ============================================================
     * LISTAR CITAS DEL PACIENTE
     * ============================================================
     */
    public List<Cita> ListarPorUsuario(
        Long usuarioId
    ) {

        return citaRepository
            .findByUsuarioId(
                usuarioId
            );
    }


    /*
     * ============================================================
     * LISTAR CITAS DEL MÉDICO
     * ============================================================
     *
     * IMPORTANTE:
     *
     * Aquí utilizamos findByMedicoId(),
     * que ya existe en tu CitaRepository.
     */
    public List<Cita> ListarPorMedico(
        Long medicoId
    ) {

        return citaRepository
            .findByMedicoId(
                medicoId
            );
    }


    /*
     * ============================================================
     * LISTAR TODAS LAS CITAS
     * ============================================================
     */
    public List<Cita> listarTodos() {

        return citaRepository.findAll();
    }


    /*
     * ============================================================
     * RESERVAR CITA
     * ============================================================
     */
    @Transactional
    public String Reservar(
        Usuario usuario,
        Long disponibilidadId,
        LocalTime horaSeleccionada
    ) {

        if (
            usuario == null
            || disponibilidadId == null
            || horaSeleccionada == null
        ) {

            return "Error: faltan campos obligatorios";
        }


        /*
         * Bloqueamos la disponibilidad mientras
         * se procesa la reserva.
         *
         * Esto ayuda a evitar dos reservas simultáneas.
         */
        Optional<DisponibilidadMedica> dispOpt =
            disponibilidadRepository
                .findByIdAndActivoTrueForUpdate(
                    disponibilidadId
                );


        if (dispOpt.isEmpty()) {

            return "Error: el espacio de disponibilidad no existe o está inactivo";
        }


        DisponibilidadMedica disponibilidad =
            dispOpt.get();


        Medico medico =
            disponibilidad.getMedico();


        if (medico == null) {

            return "Error: la disponibilidad no tiene un médico asociado";
        }


        LocalDate fecha =
            disponibilidad.getFecha();


        LocalTime horaInicioDisponibilidad =
            disponibilidad.getHoraInicio();


        LocalTime horaFinDisponibilidad =
            disponibilidad.getHoraFin();


        /*
         * ========================================================
         * VALIDACIONES DE FECHA
         * ========================================================
         */
        if (fecha == null) {

            return "Error: la disponibilidad no tiene una fecha válida";
        }


        if (
            fecha.isBefore(
                LocalDate.now()
            )
        ) {

            return "Error: no se puede reservar una cita en una fecha pasada";
        }


        /*
         * ========================================================
         * VALIDACIONES DE HORARIO
         * ========================================================
         */
        if (
            horaInicioDisponibilidad == null
            || horaFinDisponibilidad == null
        ) {

            return "Error: el horario del médico no es válido";
        }


        if (
            horaSeleccionada.isBefore(
                horaInicioDisponibilidad
            )
        ) {

            return "Error: la hora seleccionada está fuera del horario disponible del médico";
        }


        LocalTime horaFinCita =
            horaSeleccionada.plusHours(
                DURACION_CITA_HORAS
            );


        if (
            horaFinCita.isAfter(
                horaFinDisponibilidad
            )
        ) {

            return "Error: la hora seleccionada está fuera del horario disponible del médico";
        }


        if (
            !horaSeleccionada.isBefore(
                horaFinDisponibilidad
            )
        ) {

            return "Error: la hora seleccionada está fuera del horario disponible del médico";
        }


        /*
         * ========================================================
         * BLOQUES DE UNA HORA
         * ========================================================
         *
         * Ejemplo:
         *
         * 08:00
         * 09:00
         * 10:00
         *
         * No:
         *
         * 08:15
         * 08:30
         */
        long minutosDesdeInicio =
            java.time.temporal.ChronoUnit.MINUTES.between(
                horaInicioDisponibilidad,
                horaSeleccionada
            );


        if (
            minutosDesdeInicio < 0
            ||
            minutosDesdeInicio
                % (DURACION_CITA_HORAS * 60) != 0
        ) {

            return "Error: la hora seleccionada no corresponde a un bloque válido";
        }


        /*
         * Si la cita es para hoy,
         * debe ser una hora futura.
         */
        if (
            fecha.equals(
                LocalDate.now()
            )
        ) {

            LocalTime ahora =
                LocalTime.now();


            if (
                !horaSeleccionada.isAfter(
                    ahora
                )
            ) {

                return "Error: no se puede reservar una cita en una hora que ya pasó";
            }
        }


        /*
         * ========================================================
         * EVITAR SOLAPAMIENTO DEL MÉDICO
         * ========================================================
         *
         * Esta es una de las reglas principales que pediste.
         *
         * Una cita PENDIENTE ya ocupa el horario.
         */
        boolean medicoOcupado =
            citaRepository
                .existsByMedicoIdAndFechaAndHoraAndEstadoIn(
                    medico.getId(),
                    fecha,
                    horaSeleccionada,
                    ESTADOS_QUE_OCUPAN_HORARIO
                );


        if (medicoOcupado) {

            return "Error: ese horario ya fue reservado por otro paciente";
        }


        /*
         * ========================================================
         * EVITAR SOLAPAMIENTO DEL PACIENTE
         * ========================================================
         *
         * El mismo paciente tampoco puede tener
         * dos citas activas simultáneamente.
         */
        List<Cita> citasPacienteEnHorario =
            citaRepository
                .findActivaCitasByUsuarioAndFechaAndHoraRange(
                    usuario.getId(),
                    fecha,
                    horaSeleccionada,
                    horaFinCita
                );


        boolean pacienteOcupado =
            citasPacienteEnHorario
                .stream()
                .anyMatch(
                    c ->
                        "PENDIENTE".equalsIgnoreCase(
                            c.getEstado()
                        )
                        ||
                        "CONFIRMADA".equalsIgnoreCase(
                            c.getEstado()
                        )
                );


        if (pacienteOcupado) {

            return "Error: ya tienes otra cita agendada en ese horario";
        }


        /*
         * ========================================================
         * CREAR LA CITA
         * ========================================================
         */
        Cita cita =
            new Cita();


        cita.setUsuario(
            usuario
        );


        cita.setMedico(
            medico
        );


        cita.setDisponibilidad(
            disponibilidad
        );


        cita.setFecha(
            fecha
        );


        cita.setHora(
            horaSeleccionada
        );


        /*
         * Una cita siempre comienza PENDIENTE.
         *
         * Desde este momento ya bloquea el horario.
         */
        cita.setEstado(
            "PENDIENTE"
        );


        cita.setFechaCreacion(
            LocalDateTime.now()
        );


        citaRepository.save(
            cita
        );


        logger.info(
            "Cita creada. Usuario: {}, Médico: {}, Fecha: {}, Hora: {}, Disponibilidad: {}",
            usuario.getId(),
            medico.getId(),
            fecha,
            horaSeleccionada,
            disponibilidadId
        );


        /*
         * Avisamos al médico.
         */
        notificacionService
            .enviarNotificacionPendienteMedico(
                cita
            );


        return "Cita Reservada";
    }


    /*
     * ============================================================
     * COMPROBAR HORARIO OCUPADO
     * ============================================================
     */
    public boolean estaHorarioOcupado(
        Long medicoId,
        LocalDate fecha,
        LocalTime hora
    ) {

        if (
            medicoId == null
            || fecha == null
            || hora == null
        ) {

            return true;
        }


        return citaRepository
            .existsByMedicoIdAndFechaAndHoraAndEstadoIn(
                medicoId,
                fecha,
                hora,
                ESTADOS_QUE_OCUPAN_HORARIO
            );
    }


    /*
     * ============================================================
     * OBTENER CITAS QUE BLOQUEAN HORARIOS
     * ============================================================
     */
    public List<Cita> obtenerCitasOcupadasDelMedico(
        Long medicoId,
        LocalDate fecha
    ) {

        return citaRepository
            .findByMedicoIdAndFechaAndEstadoInOrderByHoraAsc(
                medicoId,
                fecha,
                ESTADOS_QUE_OCUPAN_HORARIO
            );
    }


    /*
     * ============================================================
     * CANCELAR CITA
     * ============================================================
     *
     * El paciente utiliza CANCELAR.
     *
     * El médico utilizará RECHAZAR.
     */
    @Transactional
    public String Cancelar(
        Long citaId,
        Usuario usuario
    ) {

        if (usuario == null) {

            return "Error: usuario no autenticado";
        }


        Optional<Cita> citaOpt =
            citaRepository.findById(
                citaId
            );


        if (citaOpt.isEmpty()) {

            return "Error: no existe la cita";
        }


        Cita cita =
            citaOpt.get();


        if (
            "CANCELADA".equalsIgnoreCase(
                cita.getEstado()
            )
        ) {

            return "Error: la cita ya está cancelada";
        }


        /*
         * Una cita rechazada ya está finalizada.
         */
        if (
            "RECHAZADA".equalsIgnoreCase(
                cita.getEstado()
            )
        ) {

            return "Error: una cita rechazada ya no se puede cancelar";
        }


        boolean esAdmin =
            "ADMIN".equalsIgnoreCase(
                usuario.getRol()
            );


        /*
         * Si no es ADMIN,
         * solamente el propietario puede cancelar.
         */
        if (!esAdmin) {

            if (
                cita.getUsuario() == null
                || cita.getUsuario().getId() == null
                || !cita
                    .getUsuario()
                    .getId()
                    .equals(
                        usuario.getId()
                    )
            ) {

                return "Error: no tienes permisos para cancelar esta cita";
            }
        }


        /*
         * Un paciente no puede cancelar
         * una cita que ya ocurrió.
         */
        if (!esAdmin) {

            LocalDateTime fechaHoraCita =
                LocalDateTime.of(
                    cita.getFecha(),
                    cita.getHora()
                );


            if (
                !fechaHoraCita.isAfter(
                    LocalDateTime.now()
                )
            ) {

                return "Error: no se puede cancelar citas en pasado";
            }
        }


        cita.setEstado(
            "CANCELADA"
        );


        cita.setFechaCancelacion(
            LocalDateTime.now()
        );


        citaRepository.save(
            cita
        );


        logger.info(
            "Cita {} cancelada. El horario {} del {} vuelve a quedar disponible.",
            cita.getId(),
            cita.getHora(),
            cita.getFecha()
        );


        notificacionService
            .enviarNotificacionCancelacion(
                cita
            );


        return "Cita Cancelada";
    }


    /*
     * ============================================================
     * CONFIRMAR CITA
     * ============================================================
     *
     * Ahora pueden confirmar:
     *
     * ADMIN
     *
     * MEDICO propietario de la cita
     */
    @Transactional
    public String Confirmar(
        Long citaId,
        Usuario usuario
    ) {

        if (usuario == null) {

            return "Error: usuario no autenticado";
        }


        Optional<Cita> citaOpt =
            citaRepository.findById(
                citaId
            );


        if (citaOpt.isEmpty()) {

            return "Error: no existe la cita";
        }


        Cita cita =
            citaOpt.get();


        /*
         * Comprobamos que el médico
         * realmente sea dueño de esa cita.
         */
        if (
            !puedeGestionarCitaComoMedicoOAdmin(
                cita,
                usuario
            )
        ) {

            return "Error: no tienes permisos para confirmar esta cita";
        }


        /*
         * Solamente PENDIENTE puede confirmarse.
         */
        if (
            !"PENDIENTE".equalsIgnoreCase(
                cita.getEstado()
            )
        ) {

            return "Error: la cita no está pendiente";
        }


        cita.setEstado(
            "CONFIRMADA"
        );


        citaRepository.save(
            cita
        );


        logger.info(
            "Cita {} confirmada por {} con rol {}",
            cita.getId(),
            usuario.getEmail(),
            usuario.getRol()
        );


        notificacionService
            .enviarNotificacionConfirmacion(
                cita
            );


        return "Cita Confirmada";
    }


    /*
     * ============================================================
     * RECHAZAR CITA
     * ============================================================
     *
     * Esta es la acción nueva para el médico.
     *
     * Solamente puede rechazarse una cita PENDIENTE.
     *
     * Al pasar a RECHAZADA,
     * el horario automáticamente deja de estar ocupado.
     */
    @Transactional
    public String Rechazar(
        Long citaId,
        Usuario usuario
    ) {

        if (usuario == null) {

            return "Error: usuario no autenticado";
        }


        Optional<Cita> citaOpt =
            citaRepository.findById(
                citaId
            );


        if (citaOpt.isEmpty()) {

            return "Error: no existe la cita";
        }


        Cita cita =
            citaOpt.get();


        /*
         * El médico solamente puede rechazar
         * citas que realmente le pertenezcan.
         */
        if (
            !puedeGestionarCitaComoMedicoOAdmin(
                cita,
                usuario
            )
        ) {

            return "Error: no tienes permisos para rechazar esta cita";
        }


        /*
         * Solo se rechazan solicitudes pendientes.
         */
        if (
            !"PENDIENTE".equalsIgnoreCase(
                cita.getEstado()
            )
        ) {

            return "Error: solo se pueden rechazar citas pendientes";
        }


        cita.setEstado(
            "RECHAZADA"
        );


        citaRepository.save(
            cita
        );


        logger.info(
            "Cita {} rechazada por {}. El horario {} del {} queda nuevamente disponible.",
            cita.getId(),
            usuario.getEmail(),
            cita.getHora(),
            cita.getFecha()
        );


        notificacionService
            .enviarNotificacionRechazo(
                cita
            );


        return "Cita Rechazada";
    }


    /*
     * ============================================================
     * VALIDAR PERMISOS DEL MÉDICO
     * ============================================================
     */
    private boolean puedeGestionarCitaComoMedicoOAdmin(
        Cita cita,
        Usuario usuario
    ) {

        /*
         * ADMIN puede gestionar cualquier cita.
         */
        if (
            "ADMIN".equalsIgnoreCase(
                usuario.getRol()
            )
        ) {

            return true;
        }


        /*
         * Si no es MEDICO,
         * no tiene permiso.
         */
        if (
            !"MEDICO".equalsIgnoreCase(
                usuario.getRol()
            )
        ) {

            return false;
        }


        /*
         * Comprobaciones de seguridad.
         */
        if (
            cita == null
            || cita.getMedico() == null
            || cita.getMedico().getEmail() == null
            || usuario.getEmail() == null
        ) {

            return false;
        }


        /*
         * El correo del usuario MEDICO debe ser
         * el mismo correo del médico de la cita.
         */
        return cita
            .getMedico()
            .getEmail()
            .equalsIgnoreCase(
                usuario.getEmail()
            );
    }


    /*
     * ============================================================
     * FILTRAR CITAS
     * ============================================================
     */
    public List<Cita> Filtrar(
        String estado,
        Long medicoId,
        String especialidad,
        LocalDate fechaDesde,
        LocalDate fechaHasta
    ) {

        if (
            estado != null
            && estado.isBlank()
        ) {

            estado =
                null;
        }


        if (
            especialidad != null
            && especialidad.isBlank()
        ) {

            especialidad =
                null;
        }


        return citaRepository.filtrar(
            estado,
            medicoId,
            especialidad,
            fechaDesde,
            fechaHasta
        );
    }


    /*
     * ============================================================
     * OBTENER CITA POR ID
     * ============================================================
     */
    public Optional<Cita> obtenerCitaPorId(
        Long citaId
    ) {

        return citaRepository
            .findById(
                citaId
            );
    }
}