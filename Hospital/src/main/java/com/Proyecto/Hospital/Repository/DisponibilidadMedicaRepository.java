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
    
    List<DisponibilidadMedica> findByMedicoIdAndActivoTrue(Long medicoId);
    
    List<DisponibilidadMedica> findByMedicoIdAndFechaAndActivoTrue(Long medicoId, LocalDate fecha);
    
    Optional<DisponibilidadMedica> findByIdAndActivoTrue(Long id);
    
    @Query("SELECT d FROM DisponibilidadMedica d WHERE d.medico.id = :medicoId AND d.fecha = :fecha AND d.horaInicio = :hora AND d.activo = true")
    Optional<DisponibilidadMedica> findByMedicoAndFechaAndHora(
        @Param("medicoId") Long medicoId,
        @Param("fecha") LocalDate fecha,
        @Param("hora") LocalTime hora
    );
    
    @Query("SELECT d FROM DisponibilidadMedica d WHERE d.medico.id = :medicoId AND d.fecha >= :fechaDesde AND d.fecha <= :fechaHasta AND d.activo = true")
    List<DisponibilidadMedica> findByMedicoAndFechaRango(
        @Param("medicoId") Long medicoId,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );
}
