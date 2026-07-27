package com.Proyecto.Hospital.Repository;

import com.Proyecto.Hospital.Model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalTime;

public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByUsuarioId(Long usuarioId);

    Optional<Cita> findByMedicoIdAndFechaAndHoraAndEstadoNot(Long medicoId, LocalDate fecha, LocalTime hora, String estado);

    List<Cita> findByEstado(String estado);
}
