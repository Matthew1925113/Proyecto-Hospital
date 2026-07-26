package com.Proyecto.Hospital.Service;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.stereotype.Service;
import com.Proyecto.Hospital.Model.Paciente;
import com.Proyecto.Hospital.Model.Telefono;
import com.Proyecto.Hospital.Repository.TelefonoRepository;

@Service
public class TelefonoService {
    @Autowired
    private TelefonoRepository repository;

    @Autowired
    private PacienteService pacienteService;

    public void agregarTelefono(Long pacienteId, String numero) {
        Paciente paciente = pacienteService.ObtenerPaciente(pacienteId);
        if (paciente == null) {
            throw new RuntimeException("Paciente no encontrado con ID: " + pacienteId);
        }

        if (numero == null || numero.isBlank()) {
            return;
        }
        Telefono telefono = new Telefono();
        telefono.setNumero(numero);
        telefono.setPaciente(paciente);
        repository.save(telefono);
    }

        
    
}
