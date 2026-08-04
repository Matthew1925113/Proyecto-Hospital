package com.Proyecto.Hospital.Repository;

import com.Proyecto.Hospital.Model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalTime;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByUsuarioId(Long usuarioId);

    @Query("SELECT c FROM Cita c WHERE c.medico.id = :medicoId AND c.fecha = :fecha AND c.hora = :hora AND c.estado != :estado")
    Optional<Cita> findByMedicoIdAndFechaAndHoraAndEstadoNot(
        @Param("medicoId") Long medicoId,
        @Param("fecha") LocalDate fecha,
        @Param("hora") LocalTime hora,
        @Param("estado") String estado
    );

    List<Cita> findByEstado(String estado);

    @Query("SELECT c FROM Cita c WHERE " +
        "(:estado IS NULL OR c.estado = :estado) " +
        "AND (:medicoId IS NULL OR c.medico.id = :medicoId) " +
        "AND (:especialidad IS NULL OR c.medico.especialidad = :especialidad) " +
        "AND (:fechaDesde IS NULL OR c.fecha >= :fechaDesde) " +
        "AND (:fechaHasta IS NULL OR c.fecha <= :fechaHasta)")
    List<Cita> filtrar(@Param("estado") String estado,
                    @Param("medicoId") Long medicoId,
                    @Param("especialidad") String especialidad,
                    @Param("fechaDesde") LocalDate fechaDesde,
                    @Param("fechaHasta") LocalDate fechaHasta);

    List<Cita> findByMedicoId(Long medicoId);
    
    List<Cita> findByMedicoIdAndEstado(Long medicoId, String estado);
}
