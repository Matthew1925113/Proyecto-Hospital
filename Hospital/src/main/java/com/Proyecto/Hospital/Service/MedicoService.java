package com.Proyecto.Hospital.Service;

import com.Proyecto.Hospital.Model.Medico;
import com.Proyecto.Hospital.Model.Cita;
import com.Proyecto.Hospital.Model.DisponibilidadMedica;
import com.Proyecto.Hospital.Repository.MedicoRepository;
import com.Proyecto.Hospital.Repository.CitaRepository;
import com.Proyecto.Hospital.Repository.DisponibilidadMedicaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MedicoService {
    
    private static final Logger logger = LoggerFactory.getLogger(MedicoService.class);
    
    private final MedicoRepository repository;
    private final CitaRepository citaRepository;
    private final DisponibilidadMedicaRepository disponibilidadRepository;

    public MedicoService(MedicoRepository repository, CitaRepository citaRepository,
                        DisponibilidadMedicaRepository disponibilidadRepository) {
        this.repository = repository;
        this.citaRepository = citaRepository;
        this.disponibilidadRepository = disponibilidadRepository;
    }
    
    public List<Medico> ListarMedicos() {
        return repository.findAll();
    }

    public Medico ObtenerMedico(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public String GuardarMedico(Medico medico) {
        boolean esNuevo = medico.getId() == null;
        
        if (esNuevo) {
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
        
        Medico medicoGuardado = repository.save(medico);
        
        // Si es nuevo médico, crear disponibilidades para los próximos 30 días
        if (esNuevo) {
            crearDisponibilidadesIniciales(medicoGuardado);
        }
        
        logger.info("Médico guardado: {} con disponibilidades iniciales creadas", medicoGuardado.getId());
        return "El medico ha sido guardado correctamente";
    }

    @Transactional
    private void crearDisponibilidadesIniciales(Medico medico) {
        String horarioStr = medico.getDisponibilidadHoraria();
        if (horarioStr == null || horarioStr.isBlank()) {
            logger.warn("Médico {} sin horario definido", medico.getId());
            return;
        }

        try {
            LocalDate hoy = LocalDate.now();

            // Crear disponibilidades para los próximos 30 días
            for (int i = 0; i < 30; i++) {
                LocalDate fecha = hoy.plusDays(i);

                // Parsear horarios (formato: "HH:mm-HH:mm" o puede tener multiples rangos separados por coma)
                String[] rangos = horarioStr.split(",");
                
                for (String rango : rangos) {
                    rango = rango.trim();
                    
                    if (rango.isEmpty()) continue;
                    
                    String[] partes = rango.split("-");
                    if (partes.length != 2) {
                        logger.warn("Formato de horario inválido: {}", rango);
                        continue;
                    }

                    try {
                        LocalTime inicio = LocalTime.parse(partes[0].trim());
                        LocalTime fin = LocalTime.parse(partes[1].trim());

                        // Validar que inicio sea antes que fin
                        if (inicio.isAfter(fin) || inicio.equals(fin)) {
                            logger.warn("Horario inválido para médico {}: {} - {}", medico.getId(), inicio, fin);
                            continue;
                        }

                        DisponibilidadMedica disponibilidad = new DisponibilidadMedica();
                        disponibilidad.setMedico(medico);
                        disponibilidad.setFecha(fecha);
                        disponibilidad.setHoraInicio(inicio);
                        disponibilidad.setHoraFin(fin);
                        disponibilidad.setActivo(true);

                        disponibilidadRepository.save(disponibilidad);
                    } catch (Exception e) {
                        logger.error("Error al parsear horario: {}", rango, e);
                    }
                }
            }

            logger.info("Disponibilidades iniciales creadas para médico {} - 30 días", medico.getId());
        } catch (Exception e) {
            logger.error("Error al crear disponibilidades iniciales para médico {}", medico.getId(), e);
            throw new RuntimeException("Error al crear disponibilidades: " + e.getMessage());
        }
    }

    public String EliminarMedico(Long id) {
        List<Cita> citas = citaRepository.findByMedicoId(id);
        if (!citas.isEmpty()) {
            return "No se puede eliminar el medico porque tiene citas asociadas";
        }
        repository.deleteById(id);
        return "El medico ha sido eliminado correctamente";
    }
}
