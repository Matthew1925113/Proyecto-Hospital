package com.Proyecto.Hospital.Service;

import com.Proyecto.Hospital.Model.Cita;
import com.Proyecto.Hospital.Model.Medico;
import com.Proyecto.Hospital.Model.Usuario;
import com.Proyecto.Hospital.Repository.CitaRepository;
import com.Proyecto.Hospital.Repository.MedicoRepository;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;
import java.util.List;
import java.time.LocalTime;

@Service
public class CitaService {
    private final CitaRepository citaRepository;
    private final MedicoRepository medicoRepository;
    
    public CitaService(CitaRepository citaRepository, MedicoRepository medicoRepository) {
        this.citaRepository = citaRepository;
        this.medicoRepository = medicoRepository;
    }

    public List<Cita> ListarPorUsuario(Long usuarioId) {
        return citaRepository.findByUsuarioId(usuarioId);
    }

    public List<Cita> listarTodos() {
        return citaRepository.findAll();
    }

    private boolean horaEstaDisponible(String disponibilidadHorario , LocalTime hora) {
        String[] horas = disponibilidadHorario.split(",");
        for (String horaStr : horas) {
            String[] rango = horaStr.trim().split("-");
            LocalTime inicio = LocalTime.parse(rango[0].trim());
            LocalTime fin = LocalTime.parse(rango[1].trim());
            if (!hora.isBefore(inicio) && hora.isBefore(fin)) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public String Reservar (Usuario usuario, Long medicoId, LocalDate fecha, LocalTime hora) {
        //Validr caampos obligatorios 
        if (  medicoId == null || fecha == null || hora == null) {
            return "Error: faltan campos obligatorios";
        }

        //Validar que el medico exista
        Medico medicoExistente = medicoRepository.findById(medicoId).orElse(null);
        if (medicoExistente == null) {
            return "Error: medico no existe";
        }

        //No crear citas en pasado 
        LocalDateTime fechaHoraCita = LocalDateTime.of(fecha, hora);
        if (fechaHoraCita.isBefore(LocalDateTime.now())) {
            return "Error: no se puede reservar citas en pasado";
        }
        
        //Validar disponibilidad de horario
        if (!horaEstaDisponible(medicoExistente.getDisponibilidadHoraria(), hora)) {
            return "Error: el horario no está disponible para el médico seleccionado";
        }

        //Verificar espacio no ocupado
        Optional<Cita> Cita = citaRepository.findByMedicoIdAndFechaAndHoraAndEstadoNot(medicoId, fecha, hora, "CANCELADA");
        if (Cita.isPresent()) {
            return "Error: el espacio ya está ocupado";
        }

        //Verificar que los usarios no tengan citas a la misma hora 
        List<Cita> citas = citaRepository.findByUsuarioId(usuario.getId());
        for (Cita c : citas) {
            if (c.getFecha().equals(fecha) && c.getHora().equals(hora) && !c.getEstado().equals("CANCELADA")) {
                return "Error: ya existe una cita en la misma hora";
            }
        }

        // Crear cita
        Cita cita = new Cita();
        cita.setUsuario(usuario);
        cita.setMedico(medicoExistente);
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setEstado("PENDIENTE");
        citaRepository.save(cita);
        return "Cita Reservada";
    }

    @Transactional
    public String Cancelar(Long citaId, Usuario usuario) {
        Optional<Cita> citaOpt = citaRepository.findById(citaId);
        if (citaOpt.isEmpty()) {
            return "Error: no existe la cita";
        }

        Cita cita = citaOpt.get();
        if ("CANCELADA".equals(cita.getEstado())) {
            return "Error: la cita ya está cancelada";
        }

        boolean esAdmin = "ADMIN".equalsIgnoreCase(usuario.getRol());
        if (!esAdmin) {
            if (!cita.getUsuario().getId().equals(usuario.getId())) {
                return "Error: no tienes permisos para cancelar esta cita";
            }
        }

        if (!esAdmin) {
            LocalDateTime fechaHoraCita = LocalDateTime.of(cita.getFecha(), cita.getHora());
            if (!fechaHoraCita.isAfter(LocalDateTime.now())) {
                return "Error: no se puede cancelar citas en pasado";
            }
        }

        cita.setEstado("CANCELADA");
        citaRepository.save(cita);
        return "Cita Cancelada";
    }

    @Transactional
    public String Confirmar(Long citaId, Usuario usuario) {
        if (!"ADMIN".equalsIgnoreCase(usuario.getRol())) {
            return "Error: no tienes permisos para confirmar esta cita";
        }

        Optional<Cita> citaOpt = citaRepository.findById(citaId);
        if (citaOpt.isEmpty()) {
            return "Error: no existe la cita";
        }

        Cita cita = citaOpt.get();
        if (!"PENDIENTE".equals(cita.getEstado())) {
            return "Error: la cita no está pendiente";
        }

        cita.setEstado("CONFIRMADA");
        citaRepository.save(cita);
        return "Cita Confirmada";
    }


}

        

