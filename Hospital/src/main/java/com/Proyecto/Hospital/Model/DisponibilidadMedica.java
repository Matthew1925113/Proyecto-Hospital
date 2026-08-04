package com.Proyecto.Hospital.Model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "disponibilidad_medica", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"medico_id", "fecha", "hora_inicio"})
})
public class DisponibilidadMedica {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;
    
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    
    @Column(nullable = false)
    private Boolean activo = true;
    
    private String descripcion;
    
    public DisponibilidadMedica() {}
    
    public DisponibilidadMedica(Medico medico, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        this.medico = medico;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.activo = true;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Medico getMedico() {
        return medico;
    }
    
    public void setMedico(Medico medico) {
        this.medico = medico;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    public LocalTime getHoraInicio() {
        return horaInicio;
    }
    
    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }
    
    public LocalTime getHoraFin() {
        return horaFin;
    }
    
    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    // Utility method to check if a time is within this availability slot
    public boolean contieneHora(LocalTime hora) {
        return !hora.isBefore(this.horaInicio) && hora.isBefore(this.horaFin);
    }
}
