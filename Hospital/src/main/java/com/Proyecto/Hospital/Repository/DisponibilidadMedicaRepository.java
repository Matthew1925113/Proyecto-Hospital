package com.Proyecto.Hospital.Repository;

import com.Proyecto.Hospital.Model.DisponibilidadMedica;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DisponibilidadMedicaRepository
        extends JpaRepository<DisponibilidadMedica, Long> {

    Optional<DisponibilidadMedica> findByIdAndActivoTrue(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT d
        FROM DisponibilidadMedica d
        WHERE d.id = :id
          AND d.activo = true
        """)
    Optional<DisponibilidadMedica> findByIdAndActivoTrueForUpdate(
        @Param("id") Long id
    );

    List<DisponibilidadMedica> findByMedicoIdAndActivoTrue(Long medicoId);

    List<DisponibilidadMedica> findByMedicoIdAndFechaAndActivoTrue(
        Long medicoId,
        LocalDate fecha
    );

    @Query("""
        SELECT d
        FROM DisponibilidadMedica d
        WHERE d.medico.id = :medicoId
          AND d.fecha >= :desde
          AND d.fecha <= :hasta
          AND d.activo = true
        ORDER BY d.fecha ASC, d.horaInicio ASC
        """)
    List<DisponibilidadMedica> findByMedicoIdAndFechaRangeAndActivoTrue(
        @Param("medicoId") Long medicoId,
        @Param("desde") LocalDate desde,
        @Param("hasta") LocalDate hasta
    );

    @Query("""
        SELECT d
        FROM DisponibilidadMedica d
        WHERE d.activo = true
        ORDER BY d.fecha ASC, d.horaInicio ASC
        """)
    List<DisponibilidadMedica> findAllActivas();

    @Query("""
        SELECT d
        FROM DisponibilidadMedica d
        WHERE d.medico.id = :medicoId
          AND d.fecha = :fecha
          AND d.activo = true
        ORDER BY d.horaInicio ASC
        """)
    List<DisponibilidadMedica> findByMedicoAndFechaActivas(
        @Param("medicoId") Long medicoId,
        @Param("fecha") LocalDate fecha
    );
}