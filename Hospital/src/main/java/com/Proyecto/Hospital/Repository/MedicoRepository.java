package com.Proyecto.Hospital.Repository;

import com.Proyecto.Hospital.Model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicoRepository extends JpaRepository<Medico, Long> {
    
}