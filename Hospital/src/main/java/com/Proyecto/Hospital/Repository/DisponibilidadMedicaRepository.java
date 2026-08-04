package com.Proyecto.Hospital.Repository;

import com.Proyecto.Hospital.Model.DisponibilidadMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface DisponibilidadMedicaRepository extends JpaRepository<DisponibilidadMedica, Long> {
    
    /**
     * Obtener disponibilidad por ID solo si está activa
     */
    Optional<DisponibilidadMedica> findByIdAndActivoTrue(Long id);
    
    /**
     * Listar todas las disponibilidades activas de un médico
     */
    List<DisponibilidadMedica> findByMedicoIdAndActivoTrue(Long medicoId);
    
    /**
     * Listar disponibilidades activas de un médico en una fecha específica
     */
    List<DisponibilidadMedica> findByMedicoIdAndFechaAndActivoTrue(Long medicoId, LocalDate fecha);
    
    /**
     * Listar disponibilidades activas de un médico en un rango de fechas
     */
    @Query("SELECT d FROM DisponibilidadMedica d WHERE d.medico.id = :medicoId AND d.fecha >= :desde AND d.fecha <= :hasta AND d.activo = true ORDER BY d.fecha ASC, d.horaInicio ASC")
    List<DisponibilidadMedica> findByMedicoIdAndFechaRangeAndActivoTrue(
        @Param("medicoId") Long medicoId,
        @Param("desde") LocalDate desde,
        @Param("hasta") LocalDate hasta
    );
    
    /**
     * Listar TODAS las disponibilidades activas (para cargar en formulario)
     */
    @Query("SELECT d FROM DisponibilidadMedica d WHERE d.activo = true ORDER BY d.fecha ASC, d.horaInicio ASC")
    List<DisponibilidadMedica> findAllActivas();
    
    /**
     * Obtener disponibilidades activas por médico en una fecha
     */
    @Query("SELECT d FROM DisponibilidadMedica d WHERE d.medico.id = :medicoId AND d.fecha = :fecha AND d.activo = true ORDER BY d.horaInicio ASC")
    List<DisponibilidadMedica> findByMedicoAndFechaActivas(
        @Param("medicoId") Long medicoId,
        @Param("fecha") LocalDate fecha
    );
}
