package com.Proyecto.Hospital.Service;

import com.Proyecto.Hospital.Model.Cita;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class NotificacionService {

    private static final Logger logger =
        LoggerFactory.getLogger(NotificacionService.class);

    public void enviarNotificacionPendienteMedico(Cita cita) {

        if (cita == null
            || cita.getMedico() == null
            || cita.getUsuario() == null) {

            logger.warn(
                "No se pudo generar la notificación de cita pendiente porque faltan datos."
            );

            return;
        }


        String mensaje = String.format(
            "[NOTIFICACIÓN MÉDICO] Tienes una nueva cita pendiente de gestionar.%n" +
            "Paciente: %s%n" +
            "Fecha: %s%n" +
            "Hora: %s%n" +
            "ID Cita: %d%n" +
            "Acciones disponibles: Confirmar o Rechazar",
            cita.getUsuario().getNombre(),
            cita.getFecha(),
            cita.getHora(),
            cita.getId()
        );


        logger.info(
            "ENVIANDO NOTIFICACIÓN AL MÉDICO: {}",
            cita.getMedico().getEmail()
        );

        logger.info(mensaje);

    }

    public void enviarNotificacionConfirmacion(Cita cita) {

        if (cita == null
            || cita.getMedico() == null
            || cita.getUsuario() == null) {

            logger.warn(
                "No se pudo generar la notificación de confirmación porque faltan datos."
            );

            return;
        }


        String mensaje = String.format(
            "[NOTIFICACIÓN CONFIRMACIÓN] Tu cita ha sido confirmada.%n" +
            "Médico: %s %s%n" +
            "Especialidad: %s%n" +
            "Fecha: %s%n" +
            "Hora: %s%n" +
            "Estado: CONFIRMADA",
            cita.getMedico().getNombre(),
            cita.getMedico().getApellido(),
            cita.getMedico().getEspecialidad(),
            cita.getFecha(),
            cita.getHora()
        );


        logger.info(
            "ENVIANDO NOTIFICACIÓN AL USUARIO: {}",
            cita.getUsuario().getEmail()
        );

        logger.info(mensaje);

    }

    public void enviarNotificacionRechazo(Cita cita) {

        if (cita == null
            || cita.getMedico() == null
            || cita.getUsuario() == null) {

            logger.warn(
                "No se pudo generar la notificación de rechazo porque faltan datos."
            );

            return;
        }


        String mensaje = String.format(
            "[NOTIFICACIÓN RECHAZO] Tu solicitud de cita fue rechazada.%n" +
            "Médico: %s %s%n" +
            "Especialidad: %s%n" +
            "Fecha: %s%n" +
            "Hora: %s%n" +
            "Estado: RECHAZADA%n" +
            "Puedes seleccionar otro horario disponible.",
            cita.getMedico().getNombre(),
            cita.getMedico().getApellido(),
            cita.getMedico().getEspecialidad(),
            cita.getFecha(),
            cita.getHora()
        );


        logger.info(
            "ENVIANDO NOTIFICACIÓN DE RECHAZO AL USUARIO: {}",
            cita.getUsuario().getEmail()
        );

        logger.info(mensaje);

    }

    public void enviarNotificacionCancelacion(Cita cita) {

        if (cita == null
            || cita.getMedico() == null
            || cita.getUsuario() == null) {

            logger.warn(
                "No se pudo generar la notificación de cancelación porque faltan datos."
            );

            return;
        }


        String mensaje = String.format(
            "[NOTIFICACIÓN CANCELACIÓN] La cita ha sido cancelada.%n" +
            "Médico: %s %s%n" +
            "Fecha: %s%n" +
            "Hora: %s%n" +
            "Estado: CANCELADA%n" +
            "El horario vuelve a quedar disponible.",
            cita.getMedico().getNombre(),
            cita.getMedico().getApellido(),
            cita.getFecha(),
            cita.getHora()
        );


        logger.info(
            "ENVIANDO NOTIFICACIÓN DE CANCELACIÓN AL USUARIO: {}",
            cita.getUsuario().getEmail()
        );

        logger.info(mensaje);


        logger.info(
            "ENVIANDO NOTIFICACIÓN DE CANCELACIÓN AL MÉDICO: {}",
            cita.getMedico().getEmail()
        );

        logger.info(mensaje);

    }
}