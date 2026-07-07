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

    public void GuardarMedico(Medico medico) {
        repository.save(medico);
    }

    public void EliminarMedico(Long id) {
        repository.deleteById(id);
    }
}