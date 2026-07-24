package com.Proyecto.Hospital.Repository;

import com.Proyecto.Hospital.Model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MedicoRepository extends JpaRepository<Medico, Long> {
    Optional<Medico> findByEmail(String email);
    Optional<Medico> findByCedula(String cedula);
    
}