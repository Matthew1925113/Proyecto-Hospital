package com.Proyecto.Hospital.Repository;

import com.Proyecto.Hospital.Model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    
}
