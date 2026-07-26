package com.Proyecto.Hospital.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Proyecto.Hospital.Model.Telefono;

public interface TelefonoRepository extends JpaRepository<Telefono, Long> {
    
}
