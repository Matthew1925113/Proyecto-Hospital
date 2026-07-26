package com.Proyecto.Hospital.Service;

import com.Proyecto.Hospital.Model.Paciente;
import com.Proyecto.Hospital.Repository.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PacienteService {
    
    private final PacienteRepository repository;

    public PacienteService(PacienteRepository repository) {
        this.repository = repository;
    }
    
    public List<Paciente> ListarPacientes() {
        return repository.findAll();
    }

    public Paciente ObtenerPaciente(Long id) {
        return repository.findById(id).orElse(null);
    }

    public String GuardarPaciente(Paciente paciente) {
        if (paciente.getId() == null) {
            // Nuevo paciente, realizar operaciones adicionales si es necesario
            if (repository.findByEmail(paciente.getEmail()).isPresent()) {
                return "El email ya esta en uso";
            }
            else if (repository.findByCedula(paciente.getCedula()).isPresent()) {
                return "La cedula ya esta en uso";
            }
        } else {
            Paciente existente = repository.findByEmail(paciente.getEmail()).orElse(null);
            if (existente != null && !existente.getId().equals(paciente.getId())) {
                return "El email ya esta en uso";
            }
            Paciente existente2 = repository.findByCedula(paciente.getCedula()).orElse(null);
            if (existente2 != null && !existente2.getId().equals(paciente.getId())) {
                return "La cedula ya esta en uso";
            }
            Paciente dbPaciente = repository.findById(paciente.getId()).orElse(null);
            if (dbPaciente != null) {
                paciente.setTelefono(dbPaciente.getTelefono());
            }
        }

        paciente.getTelefono().removeIf(telefono -> telefono.getNumero() == null || telefono.getNumero().isBlank());
        paciente.getTelefono().forEach(telefono -> telefono.setPaciente(paciente));
        repository.save(paciente);
        return "El paciente ha sido guardado correctamente";
}




    public void EliminarPaciente(Long id) {
        repository.deleteById(id);
    }
}
