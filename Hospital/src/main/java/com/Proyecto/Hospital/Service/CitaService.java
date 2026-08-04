package com.Proyecto.Hospital.Service;

import com.Proyecto.Hospital.Model.Cita;
import com.Proyecto.Hospital.Model.Medico;
import com.Proyecto.Hospital.Model.Usuario;
import com.Proyecto.Hospital.Model.DisponibilidadMedica;
import com.Proyecto.Hospital.Repository.CitaRepository;
import com.Proyecto.Hospital.Repository.MedicoRepository;
import com.Proyecto.Hospital.Repository.DisponibilidadMedicaRepository;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CitaService {

    private final CitaRepository citaRepository;
    private final MedicoRepository medicoRepository;
    private final DisponibilidadMedicaRepository disponibilidadRepository;
    private final NotificacionService notificacionService;
    
    public CitaService(CitaRepository citaRepository, MedicoRepository medicoRepository, 
                      DisponibilidadMedicaRepository disponibilidadRepository,
                      NotificacionService notificacionService) {
        this.citaRepository = citaRepository;
        this.medicoRepository = medicoRepository;
        this.disponibilidadRepository = disponibilidadRepository;
        this.notificacionService = notificacionService;
    }

    /**
     * RN3: Estados válidos - PENDIENTE, CONFIRMADA, CANCELADA
     * RN8: Un usuario solo puede ver sus propias citas, admin ve todas
     */
    public List<Cita> ListarPorUsuario(Long usuarioId) {
        return citaRepository.findByUsuarioId(usuarioId);
    }

    public List<Cita> listarTodos() {
        return citaRepository.findAll();
    }

    /**
     * RN1: Unicidad de reserva por espacio
     * RN2: Prevención de doble reserva por concurrencia
     * RN4: Restricción temporal de creación
     * RN6: Sin solapamiento de citas para un mismo usuario
     * RN10: Validación de datos de la reserva
     * 
     * @param usuario Usuario que realiza la reserva
     * @param disponibilidadId ID del espacio de disponibilidad a reservar
     * @return Mensaje indicando éxito o error
     */
    @Transactional
    public String Reservar(Usuario usuario, Long disponibilidadId) {
        // RN10: Validar campos obligatorios
        if (usuario == null || usuario.getId() == null || disponibilidadId == null) {
            return "Error: faltan campos obligatorios";
        }

        // Obtener disponibilidad (espacio)
        Optional<DisponibilidadMedica> disponibilidadOpt = disponibilidadRepository.findByIdAndActivoTrue(disponibilidadId);
        if (disponibilidadOpt.isEmpty()) {
            return "Error: el espacio de disponibilidad no existe o está inactivo";
        }
        
        DisponibilidadMedica disponibilidad = disponibilidadOpt.get();
        Medico medico = disponibilidad.getMedico();
        LocalDate fecha = disponibilidad.getFecha();
        LocalTime hora = disponibilidad.getHoraInicio();

        // RN10: Validar que el médico exista y esté activo
        if (medico == null || medico.getId() == null) {
            return "Error: médico no válido";
        }

        // RN4: No crear citas en pasado
        LocalDateTime fechaHoraCita = LocalDateTime.of(fecha, hora);
        if (fechaHoraCita.isBefore(LocalDateTime.now())) {
            return "Error: no se puede reservar citas en fechas/horas pasadas";
        }

        // RN1 + RN2: Verificar que el espacio NO esté ocupado (con lock para concurrencia)
        // Esta verificación se hace dentro de la transacción
        Optional<Cita> citaExistente = citaRepository.findActivaCitaByMedicoAndFechaAndHora(
            medico.getId(), fecha, hora
        );
        if (citaExistente.isPresent()) {
            return "Error: el espacio ya está ocupado por otra cita activa";
        }

        // RN6: Verificar que el usuario no tenga citas solapadas
        List<Cita> citasDelUsuario = citaRepository.findActivaCitasByUsuarioAndFechaAndHoraRange(
            usuario.getId(), fecha, hora, disponibilidad.getHoraFin()
        );
        if (!citasDelUsuario.isEmpty()) {
            return "Error: ya tienes una cita en este horario con otro médico";
        }

        // Crear la cita en estado PENDIENTE (RN3)
        Cita cita = new Cita();
        cita.setUsuario(usuario);
        cita.setMedico(medico);
        cita.setDisponibilidad(disponibilidad);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setEstado("PENDIENTE");
        cita.setFechaCreacion(LocalDateTime.now());
        
        citaRepository.save(cita);
        
        // Enviar notificación al médico para que confirme o deniegue
        notificacionService.enviarNotificacionPendienteMedico(cita);
        
        return "Cita Reservada";
    }

    /**
     * RN3: Transiciones permitidas: pendiente → confirmada
     * RN8: Solo admin puede confirmar
     */
    @Transactional
    public String Confirmar(Long citaId, Usuario usuario) {
        // RN8: Solo admin puede confirmar
        if (!"ADMIN".equalsIgnoreCase(usuario.getRol())) {
            return "Error: no tienes permisos para confirmar citas";
        }

        Optional<Cita> citaOpt = citaRepository.findById(citaId);
        if (citaOpt.isEmpty()) {
            return "Error: la cita no existe";
        }

        Cita cita = citaOpt.get();
        
        // RN3: Solo se puede confirmar si está pendiente
        if (!"PENDIENTE".equals(cita.getEstado())) {
            return "Error: la cita debe estar en estado PENDIENTE para confirmar";
        }

        cita.setEstado("CONFIRMADA");
        citaRepository.save(cita);
        
        // Notificar al usuario que su cita fue confirmada
        notificacionService.enviarNotificacionConfirmacion(cita);
        
        return "Cita Confirmada";
    }

    /**
     * RN5: Restricción temporal de cancelación por el usuario
     * RN3: Transiciones permitidas: pendiente/confirmada → cancelada
     * RN7: Liberación inmediata del espacio
     * RN8: Usuario solo puede cancelar su cita si no pasó, admin siempre puede
     */
    @Transactional
    public String Cancelar(Long citaId, Usuario usuario, String motivo) {
        Optional<Cita> citaOpt = citaRepository.findById(citaId);
        if (citaOpt.isEmpty()) {
            return "Error: la cita no existe";
        }

        Cita cita = citaOpt.get();
        
        // RN8: Verificar permisos
        boolean esAdmin = "ADMIN".equalsIgnoreCase(usuario.getRol());
        if (!esAdmin && !cita.getUsuario().getId().equals(usuario.getId())) {
            return "Error: no tienes permisos para cancelar esta cita";
        }

        // RN3: No se puede cancelar una cita ya cancelada
        if ("CANCELADA".equals(cita.getEstado())) {
            return "Error: la cita ya está cancelada";
        }

        // RN5: Verificar restricción temporal para usuario regular
        if (!esAdmin) {
            LocalDateTime fechaHoraCita = LocalDateTime.of(cita.getFecha(), cita.getHora());
            if (!fechaHoraCita.isAfter(LocalDateTime.now())) {
                return "Error: no se puede cancelar una cita que ya ocurrió";
            }
        }

        // Cancelar la cita y registrar el motivo
        cita.setEstado("CANCELADA");
        cita.setFechaCancelacion(LocalDateTime.now());
        cita.setMotivoCancelacion(motivo);
        citaRepository.save(cita);
        
        // RN7: El espacio queda disponible inmediatamente (no hay acción adicional necesaria
        // porque la cita se marca como CANCELADA, no se elimina)
        
        // Notificar al médico que la cita fue cancelada
        notificacionService.enviarNotificacionCancelacion(cita);
        
        return "Cita Cancelada";
    }

    /**
     * RN8: Admin puede filtrar todas las citas, usuario solo ve las suyas
     */
    public List<Cita> Filtrar(String estado, Long medicoId, String especialidad, LocalDate fechaDesde, LocalDate fechaHasta) {
        if (estado != null && estado.isBlank()) estado = null;
        if (especialidad != null && especialidad.isBlank()) especialidad = null;
        return citaRepository.filtrar(estado, medicoId, especialidad, fechaDesde, fechaHasta);
    }
    
    /**
     * Obtener citas pendientes para un médico
     */
    public List<Cita> obtenerCitasPendientesPorMedico(Long medicoId) {
        return citaRepository.findByMedicoIdAndEstado(medicoId, "PENDIENTE");
    }
    
    /**
     * Obtener una cita por ID
     */
    public Optional<Cita> obtenerCitaPorId(Long citaId) {
        return citaRepository.findById(citaId);
    }
}
