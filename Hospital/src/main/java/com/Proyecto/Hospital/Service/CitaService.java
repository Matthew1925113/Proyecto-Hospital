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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CitaService {

    private static final Logger logger = LoggerFactory.getLogger(CitaService.class);
    private static final int DURACION_CITA_HORAS = 1; // Duración aproximada de cada cita
    
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

    public List<Cita> ListarPorUsuario(Long usuarioId) {
        return citaRepository.findByUsuarioId(usuarioId);
    }

    public List<Cita> listarTodos() {
        return citaRepository.findAll();
    }

    @Transactional
    public String Reservar(Usuario usuario, Long disponibilidadId) {
        if (usuario == null || disponibilidadId == null) {
            return "Error: faltan campos obligatorios";
        }

        Optional<DisponibilidadMedica> dispOpt = disponibilidadRepository.findByIdAndActivoTrue(disponibilidadId);
        if (dispOpt.isEmpty()) {
            return "Error: el espacio de disponibilidad no existe o está inactivo";
        }

        DisponibilidadMedica disponibilidad = dispOpt.get();
        Medico medico = disponibilidad.getMedico();
        LocalDate fecha = disponibilidad.getFecha();
        LocalTime horaInicio = disponibilidad.getHoraInicio();
        LocalTime horaFin = disponibilidad.getHoraFin();

        // Validar que hay suficiente tiempo disponible (mínimo DURACION_CITA_HORAS)
        long minutosDisponibles = java.time.temporal.ChronoUnit.MINUTES.between(horaInicio, horaFin);
        if (minutosDisponibles < DURACION_CITA_HORAS * 30) {
            return "Error: no hay suficiente tiempo disponible para agendar una cita";
        }

        // Validar que no haya cita activa en ese espacio
        Optional<Cita> citaExistente = citaRepository.findActivaCitaByMedicoAndFechaAndHora(
            medico.getId(), fecha, horaInicio
        );
        if (citaExistente.isPresent()) {
            return "Error: el espacio ya está ocupado por otra cita";
        }

        // Validar que el usuario no tenga ya una cita en ese horario
        List<Cita> citasUsuario = citaRepository.findActivaCitasByUsuarioAndFechaAndHoraRange(
            usuario.getId(), fecha, horaInicio, horaFin
        );
        if (!citasUsuario.isEmpty()) {
            return "Error: ya tienes una cita pendiente o confirmada en ese horario";
        }

        // Crear cita
        Cita cita = new Cita();
        cita.setUsuario(usuario);
        cita.setMedico(medico);
        cita.setDisponibilidad(disponibilidad);
        cita.setFecha(fecha);
        cita.setHora(horaInicio);
        cita.setEstado("PENDIENTE");
        cita.setFechaCreacion(LocalDateTime.now());
        
        citaRepository.save(cita);
        logger.info("Cita creada para usuario {} en disponibilidad {}", usuario.getId(), disponibilidadId);
        
        // Reducir disponibilidad
        reducirDisponibilidad(disponibilidad, DURACION_CITA_HORAS);
        
        notificacionService.enviarNotificacionPendienteMedico(cita);
        
        return "Cita Reservada";
    }

    @Transactional
    private void reducirDisponibilidad(DisponibilidadMedica disponibilidad, int horasACitar) {
        LocalTime nuevoInicio = disponibilidad.getHoraInicio().plusHours(horasACitar);
        
        // Si el nuevo inicio llega a o supera la hora fin, desactivar
        if (!nuevoInicio.isBefore(disponibilidad.getHoraFin())) {
            disponibilidad.setActivo(false);
            logger.info("Disponibilidad {} desactivada - no hay más espacio", disponibilidad.getId());
        } else {
            disponibilidad.setHoraInicio(nuevoInicio);
            logger.info("Disponibilidad {} reducida de {} a {}", 
                disponibilidad.getId(), 
                disponibilidad.getHoraInicio(), 
                nuevoInicio);
        }
        
        disponibilidadRepository.save(disponibilidad);
    }

    @Transactional
    public String Cancelar(Long citaId, Usuario usuario) {
        Optional<Cita> citaOpt = citaRepository.findById(citaId);
        if (citaOpt.isEmpty()) {
            return "Error: no existe la cita";
        }

        Cita cita = citaOpt.get();
        if ("CANCELADA".equals(cita.getEstado())) {
            return "Error: la cita ya está cancelada";
        }

        boolean esAdmin = "ADMIN".equalsIgnoreCase(usuario.getRol());
        if (!esAdmin && !cita.getUsuario().getId().equals(usuario.getId())) {
            return "Error: no tienes permisos para cancelar esta cita";
        }

        if (!esAdmin) {
            LocalDateTime fechaHoraCita = LocalDateTime.of(cita.getFecha(), cita.getHora());
            if (!fechaHoraCita.isAfter(LocalDateTime.now())) {
                return "Error: no se puede cancelar citas en pasado";
            }
        }

        cita.setEstado("CANCELADA");
        cita.setFechaCancelacion(LocalDateTime.now());
        citaRepository.save(cita);
        
        // Restaurar disponibilidad
        restaurarDisponibilidad(cita.getDisponibilidad());
        
        notificacionService.enviarNotificacionCancelacion(cita);
        
        return "Cita Cancelada";
    }

    @Transactional
    private void restaurarDisponibilidad(DisponibilidadMedica disponibilidad) {
        // Restaurar el horario original del médico para ese día
        String horarioStr = disponibilidad.getMedico().getDisponibilidadHoraria();
        if (horarioStr != null && !horarioStr.isBlank()) {
            String[] partes = horarioStr.split(",");
            String primerRango = partes[0].trim();
            String[] horarios = primerRango.split("-");
            
            if (horarios.length == 2) {
                LocalTime horaOriginal = LocalTime.parse(horarios[0].trim());
                disponibilidad.setHoraInicio(horaOriginal);
                disponibilidad.setActivo(true);
                disponibilidadRepository.save(disponibilidad);
                logger.info("Disponibilidad {} restaurada a {}", disponibilidad.getId(), horaOriginal);
            }
        }
    }

    @Transactional
    public String Confirmar(Long citaId, Usuario usuario) {
        if (!"ADMIN".equalsIgnoreCase(usuario.getRol())) {
            return "Error: no tienes permisos para confirmar esta cita";
        }

        Optional<Cita> citaOpt = citaRepository.findById(citaId);
        if (citaOpt.isEmpty()) {
            return "Error: no existe la cita";
        }

        Cita cita = citaOpt.get();
        if (!"PENDIENTE".equals(cita.getEstado())) {
            return "Error: la cita no está pendiente";
        }

        cita.setEstado("CONFIRMADA");
        citaRepository.save(cita);
        
        notificacionService.enviarNotificacionConfirmacion(cita);
        
        return "Cita Confirmada";
    }

    public List<Cita> Filtrar(String estado, Long medicoId, String especialidad, LocalDate fechaDesde, LocalDate fechaHasta) {
        if (estado != null && estado.isBlank()) estado = null;
        if (especialidad != null && especialidad.isBlank()) especialidad = null;
        return citaRepository.filtrar(estado, medicoId, especialidad, fechaDesde, fechaHasta);
    }
    
    public Optional<Cita> obtenerCitaPorId(Long citaId) {
        return citaRepository.findById(citaId);
    }
}