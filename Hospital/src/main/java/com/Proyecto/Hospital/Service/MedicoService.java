package com.Proyecto.Hospital.Service;

import com.Proyecto.Hospital.Model.Medico;
import com.Proyecto.Hospital.Repository.MedicoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MedicoService {
    
    private final MedicoRepository repository;

    public MedicoService(MedicoRepository repository) {
        this.repository = repository;
    }
    
    public List<Medico> ListarMedicos() {
        return repository.findAll();
    }

    public Medico ObtenerMedico(Long id) {
        return repository.findById(id).orElse(null);
    }

    public String GuardarMedico(Medico medico) {
        if (medico.getId() == null) {
            // Nuevo medico, realizar operaciones adicionales si es necesario
            if (repository.findByEmail(medico.getEmail()).isPresent()) {
                return "El email ya esta en uso";
            }
            else if (repository.findByCedula(medico.getCedula()).isPresent()) {
                return "La cedula ya esta en uso";
            }
        } else {
            Medico existente = repository.findByEmail(medico.getEmail()).orElse(null);
            if (existente != null && !existente.getId().equals(medico.getId())) {
                return "El email ya esta en uso";
            }
            Medico existente2 = repository.findByCedula(medico.getCedula()).orElse(null);
            if (existente2 != null && !existente2.getId().equals(medico.getId())) {
                return "La cedula ya esta en uso";
            }
        }
        repository.save(medico);
        return "El medico ha sido guardado correctamente";
    }

    public void EliminarMedico(Long id) {
        repository.deleteById(id);
    }
}