package com.Proyecto.Hospital.Service;

import com.Proyecto.Hospital.Model.Cita;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio de notificaciones para el módulo de citas.
 * En producción, esto se integraría con un sistema de email, SMS, o push notifications.
 * Por ahora, registra las notificaciones en logs.
 */
@Service
public class NotificacionService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificacionService.class);
    
    /**
     * Enviar notificación al médico cuando se reserva una cita (estado PENDIENTE)
     * El médico debe confirmar o denegar la cita
     */
    public void enviarNotificacionPendienteMedico(Cita cita) {
        String mensaje = String.format(
            "[NOTIFICACIÓN MÉDICO] Tienes una nueva cita pendiente de confirmar.\n" +
            "Paciente: %s\n" +
            "Fecha: %s\n" +
            "Hora: %s\n" +
            "ID Cita: %d\n" +
            "Acciones: Confirmar o Denegar",
            cita.getUsuario().getNombre(),
            cita.getFecha(),
            cita.getHora(),
            cita.getId()
        );
        
        logger.info("ENVIANDO NOTIFICACIÓN AL MÉDICO: " + cita.getMedico().getEmail());
        logger.info(mensaje);
        
        // TODO: Implementar envío de email real
        // mailService.enviar(cita.getMedico().getEmail(), "Cita Pendiente de Confirmación", mensaje);
    }
    
    /**
     * Enviar notificación al usuario cuando su cita es confirmada
     */
    public void enviarNotificacionConfirmacion(Cita cita) {
        String mensaje = String.format(
            "[NOTIFICACIÓN CONFIRMACIÓN] Tu cita ha sido confirmada.\n" +
            "Médico: %s %s\n" +
            "Especialidad: %s\n" +
            "Fecha: %s\n" +
            "Hora: %s\n" +
            "Estado: CONFIRMADA",
            cita.getMedico().getNombre(),
            cita.getMedico().getApellido(),
            cita.getMedico().getEspecialidad(),
            cita.getFecha(),
            cita.getHora()
        );
        
        logger.info("ENVIANDO NOTIFICACIÓN AL USUARIO: " + cita.getUsuario().getEmail());
        logger.info(mensaje);
        
        // TODO: Implementar envío de email real
        // mailService.enviar(cita.getUsuario().getEmail(), "Cita Confirmada", mensaje);
    }
    
    /**
     * Enviar notificación cuando una cita es cancelada
     */
    public void enviarNotificacionCancelacion(Cita cita) {
        String mensaje = String.format(
            "[NOTIFICACIÓN CANCELACIÓN] Tu cita ha sido cancelada.\n" +
            "Médico: %s %s\n" +
            "Fecha: %s\n" +
            "Hora: %s\n" +
            "Por favor, agenda una nueva cita si es necesario.",
            cita.getMedico().getNombre(),
            cita.getMedico().getApellido(),
            cita.getFecha(),
            cita.getHora()
        );
        
        logger.info("ENVIANDO NOTIFICACIÓN DE CANCELACIÓN AL USUARIO: " + cita.getUsuario().getEmail());
        logger.info(mensaje);
        
        logger.info("ENVIANDO NOTIFICACIÓN DE CANCELACIÓN AL MÉDICO: " + cita.getMedico().getEmail());
        logger.info(mensaje);
        
        // TODO: Implementar envío de email real
        // mailService.enviar(cita.getUsuario().getEmail(), "Cita Cancelada", mensaje);
        // mailService.enviar(cita.getMedico().getEmail(), "Cita Cancelada por el Paciente", mensaje);
    }
}
