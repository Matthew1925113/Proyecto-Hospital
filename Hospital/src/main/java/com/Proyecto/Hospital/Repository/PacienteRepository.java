package com.Proyecto.Hospital.Repository;

import com.Proyecto.Hospital.Model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;


public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByEmail(String email);
    Optional<Paciente> findByCedula(String cedula);
    
}
