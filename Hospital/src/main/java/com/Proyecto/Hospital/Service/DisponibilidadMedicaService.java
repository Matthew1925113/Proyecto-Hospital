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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DisponibilidadMedicaService {
    
    private static final Logger logger = LoggerFactory.getLogger(DisponibilidadMedicaService.class);
    
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
     * Actualizar una disponibilidad específica
     */
    @Transactional
    public String actualizar(Long disponibilidadId, LocalTime horaInicio, LocalTime horaFin, Boolean activo) {
        Optional<DisponibilidadMedica> dispOpt = disponibilidadRepository.findById(disponibilidadId);
        if (dispOpt.isEmpty()) {
            return "Error: la disponibilidad no existe";
        }

        DisponibilidadMedica disponibilidad = dispOpt.get();
        disponibilidad.setHoraInicio(horaInicio);
        disponibilidad.setHoraFin(horaFin);
        disponibilidad.setActivo(activo);
        
        disponibilidadRepository.save(disponibilidad);
        logger.info("Disponibilidad {} actualizada", disponibilidadId);
        
        return "Disponibilidad actualizada correctamente";
    }
    
    /**
     * Actualizar disponibilidades en un rango de fechas (sin afectar otros días)
     */
    @Transactional
    public String actualizarRango(Long medicoId, LocalDate fechaDesde, LocalDate fechaHasta, 
                                 LocalTime horaInicio, LocalTime horaFin, Boolean activo) {
        if (!medicoRepository.existsById(medicoId)) {
            return "Error: el médico no existe";
        }

        LocalDate fechaActual = fechaDesde;
        int actualizadas = 0;

        while (!fechaActual.isAfter(fechaHasta)) {
            List<DisponibilidadMedica> disponibilidades = 
                disponibilidadRepository.findByMedicoIdAndFechaAndActivoTrue(medicoId, fechaActual);
            
            for (DisponibilidadMedica disp : disponibilidades) {
                disp.setHoraInicio(horaInicio);
                disp.setHoraFin(horaFin);
                disp.setActivo(activo);
                disponibilidadRepository.save(disp);
                actualizadas++;
            }

            fechaActual = fechaActual.plusDays(1);
        }

        logger.info("Se actualizaron {} disponibilidades para médico {} entre {} y {}", 
            actualizadas, medicoId, fechaDesde, fechaHasta);
        
        return "Se actualizaron " + actualizadas + " espacios de disponibilidad";
    }
    
    /**
     * Desactivar una disponibilidad específica
     */
    @Transactional
    public String desactivar(Long disponibilidadId) {
        Optional<DisponibilidadMedica> dispOpt = disponibilidadRepository.findById(disponibilidadId);
        if (dispOpt.isEmpty()) {
            return "Error: la disponibilidad no existe";
        }

        DisponibilidadMedica disponibilidad = dispOpt.get();
        disponibilidad.setActivo(false);
        disponibilidadRepository.save(disponibilidad);
        logger.info("Disponibilidad {} desactivada", disponibilidadId);
        
        return "Disponibilidad desactivada";
    }
    
    /**
     * Eliminar una disponibilidad (solo si no tiene citas asociadas)
     */
    @Transactional
    public String eliminar(Long disponibilidadId) {
        Optional<DisponibilidadMedica> dispOpt = disponibilidadRepository.findById(disponibilidadId);
        if (dispOpt.isEmpty()) {
            return "Error: la disponibilidad no existe";
        }

        disponibilidadRepository.deleteById(disponibilidadId);
        logger.info("Disponibilidad {} eliminada", disponibilidadId);
        
        return "Disponibilidad eliminada";
    }
}
