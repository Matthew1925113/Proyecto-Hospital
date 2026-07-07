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

    public void GuardarPaciente(Paciente paciente) {
        repository.save(paciente);
    }

    public void EliminarPaciente(Long id) {
        repository.deleteById(id);
    }
}
