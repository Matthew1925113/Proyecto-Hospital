package com.Proyecto.Hospital.Model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "disponibilidad_id", nullable = false)
    private DisponibilidadMedica disponibilidad;

    private LocalDate fecha;
    private LocalTime hora;
    
    @Column(nullable = false)
    private String estado; // PENDIENTE, CONFIRMADA, CANCELADA
    
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCancelacion;
    private String motivoCancelacion;

    public Cita() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }

    public DisponibilidadMedica getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(DisponibilidadMedica disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        if (!estado.matches("PENDIENTE|CONFIRMADA|CANCELADA")) {
            throw new IllegalArgumentException("Estado inválido: " + estado);
        }
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(LocalDateTime fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    // Métodos útiles
    public boolean esPendiente() {
        return "PENDIENTE".equals(this.estado);
    }

    public boolean esConfirmada() {
        return "CONFIRMADA".equals(this.estado);
    }

    public boolean esCancelada() {
        return "CANCELADA".equals(this.estado);
    }

    public boolean esActiva() {
        return "PENDIENTE".equals(this.estado) || "CONFIRMADA".equals(this.estado);
    }
}
