package com.Proyecto.Hospital.Repository;

import com.Proyecto.Hospital.Model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByUsuarioId(Long usuarioId);

    Optional<Cita> findByMedicoIdAndFechaAndHoraAndEstadoNot(
        Long medicoId,
        LocalDate fecha,
        LocalTime hora,
        String estado
    );

    List<Cita> findByEstado(String estado);

    @Query("SELECT c FROM Cita c WHERE " +
        "(:estado IS NULL OR c.estado = :estado) " +
        "AND (:medicoId IS NULL OR c.medico.id = :medicoId) " +
        "AND (:especialidad IS NULL OR c.medico.especialidad = :especialidad) " +
        "AND (:fechaDesde IS NULL OR c.fecha >= :fechaDesde) " +
        "AND (:fechaHasta IS NULL OR c.fecha <= :fechaHasta)")
    List<Cita> filtrar(
        @Param("estado") String estado,
        @Param("medicoId") Long medicoId,
        @Param("especialidad") String especialidad,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );

    List<Cita> findByMedicoId(Long medicoId);

    boolean existsByMedicoIdAndFechaAndHoraAndEstadoIn(
        Long medicoId,
        LocalDate fecha,
        LocalTime hora,
        Collection<String> estadosOcupados
    );

    List<Cita> findByMedicoIdAndFechaAndEstadoInOrderByHoraAsc(
        Long medicoId,
        LocalDate fecha,
        Collection<String> estadosOcupados
    );

    @Query("""
        SELECT c
        FROM Cita c
        WHERE c.medico.id = :medicoId
          AND c.fecha = :fecha
          AND c.hora = :hora
          AND c.estado != 'CANCELADA'
        """)
    Optional<Cita> findActivaCitaByMedicoAndFechaAndHora(
        @Param("medicoId") Long medicoId,
        @Param("fecha") LocalDate fecha,
        @Param("hora") LocalTime hora
    );

    @Query("""
        SELECT c
        FROM Cita c
        WHERE c.usuario.id = :usuarioId
          AND c.fecha = :fecha
          AND c.hora >= :horaInicio
          AND c.hora < :horaFin
          AND c.estado != 'CANCELADA'
        """)
    List<Cita> findActivaCitasByUsuarioAndFechaAndHoraRange(
        @Param("usuarioId") Long usuarioId,
        @Param("fecha") LocalDate fecha,
        @Param("horaInicio") LocalTime horaInicio,
        @Param("horaFin") LocalTime horaFin
    );
}