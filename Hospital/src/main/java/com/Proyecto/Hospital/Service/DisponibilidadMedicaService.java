package com.Proyecto.Hospital.Service;

import com.Proyecto.Hospital.Model.DisponibilidadMedica;
import com.Proyecto.Hospital.Model.Medico;
import com.Proyecto.Hospital.Repository.DisponibilidadMedicaRepository;
import com.Proyecto.Hospital.Repository.MedicoRepository;
import com.Proyecto.Hospital.Repository.CitaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class DisponibilidadMedicaService {
    
    private final DisponibilidadMedicaRepository disponibilidadRepository;
    private final MedicoRepository medicoRepository;
    private final CitaRepository citaRepository;
    
    public DisponibilidadMedicaService(DisponibilidadMedicaRepository disponibilidadRepository,
                                       MedicoRepository medicoRepository,
                                       CitaRepository citaRepository) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.medicoRepository = medicoRepository;
        this.citaRepository = citaRepository;
    }
    
    /**
     * Crear un nuevo espacio de disponibilidad
     */
    @Transactional
    public String crear(Long medicoId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String descripcion) {
        // Validar campos obligatorios
        if (medicoId == null || fecha == null || horaInicio == null || horaFin == null) {
            return "Error: faltan campos obligatorios";
        }
        
        // Validar que el médico exista
        Optional<Medico> medicoOpt = medicoRepository.findById(medicoId);
        if (medicoOpt.isEmpty()) {
            return "Error: el médico no existe";
        }
        
        // Validar que horaInicio sea menor que horaFin
        if (!horaInicio.isBefore(horaFin)) {
            return "Error: la hora de inicio debe ser menor que la hora de fin";
        }
        
        // Validar que la fecha no sea en el pasado
        if (fecha.isBefore(LocalDate.now())) {
            return "Error: no se puede crear disponibilidad en fechas pasadas";
        }
        
        DisponibilidadMedica disponibilidad = new DisponibilidadMedica();
        disponibilidad.setMedico(medicoOpt.get());
        disponibilidad.setFecha(fecha);
        disponibilidad.setHoraInicio(horaInicio);
        disponibilidad.setHoraFin(horaFin);
        disponibilidad.setDescripcion(descripcion);
        disponibilidad.setActivo(true);
        
        disponibilidadRepository.save(disponibilidad);
        return "Disponibilidad creada correctamente";
    }
    
    /**
     * Listar disponibilidades activas de un médico
     */
    public List<DisponibilidadMedica> listarPorMedico(Long medicoId) {
        return disponibilidadRepository.findByMedicoIdAndActivoTrue(medicoId);
    }
    
    /**
     * Listar disponibilidades activas de un médico en una fecha específica
     */
    public List<DisponibilidadMedica> listarPorMedicoYFecha(Long medicoId, LocalDate fecha) {
        return disponibilidadRepository.findByMedicoIdAndFechaAndActivoTrue(medicoId, fecha);
    }
    
    /**
     * Obtener disponibilidad por ID
     */
    public Optional<DisponibilidadMedica> obtenerPorId(Long id) {
        return disponibilidadRepository.findByIdAndActivoTrue(id);
    }
    
    /**
     * Desactivar una disponibilidad (cuando se elimina lógicamente)
     * RN9: No se elimina físicamente si tiene citas
     */
    @Transactional
    public String desactivar(Long disponibilidadId) {
        Optional<DisponibilidadMedica> disponibilidadOpt = disponibilidadRepository.findById(disponibilidadId);
        if (disponibilidadOpt.isEmpty()) {
            return "Error: la disponibilidad no existe";
        }
        
        DisponibilidadMedica disponibilidad = disponibilidadOpt.get();
        
        // Verificar si tiene citas activas
        // (En una implementación real, habría una FK en Cita hacia DisponibilidadMedica)
        
        disponibilidad.setActivo(false);
        disponibilidadRepository.save(disponibilidad);
        
        return "Disponibilidad desactivada";
    }
    
    /**
     * Eliminar una disponibilidad (solo si no tiene citas asociadas)
     */
    @Transactional
    public String eliminar(Long disponibilidadId) {
        Optional<DisponibilidadMedica> disponibilidadOpt = disponibilidadRepository.findById(disponibilidadId);
        if (disponibilidadOpt.isEmpty()) {
            return "Error: la disponibilidad no existe";
        }
        
        DisponibilidadMedica disponibilidad = disponibilidadOpt.get();
        
        // Verificar si tiene citas
        long citasAsociadas = citaRepository.findAll().stream()
            .filter(c -> c.getDisponibilidad().getId().equals(disponibilidadId))
            .filter(c -> !c.esCancelada())
            .count();
        
        if (citasAsociadas > 0) {
            return "Error: no se puede eliminar una disponibilidad que tiene citas asociadas";
        }
        
        disponibilidadRepository.deleteById(disponibilidadId);
        return "Disponibilidad eliminada";
    }
}
