package com.Proyecto.Hospital.Service;

import com.Proyecto.Hospital.Model.Cita;
import com.Proyecto.Hospital.Model.DisponibilidadMedica;
import com.Proyecto.Hospital.Model.Medico;

import com.Proyecto.Hospital.Repository.CitaRepository;
import com.Proyecto.Hospital.Repository.DisponibilidadMedicaRepository;
import com.Proyecto.Hospital.Repository.MedicoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import java.util.List;
import java.util.Optional;

@Service
public class DisponibilidadMedicaService {

    private static final Logger logger =
        LoggerFactory.getLogger(DisponibilidadMedicaService.class);


    private final DisponibilidadMedicaRepository disponibilidadRepository;

    private final MedicoRepository medicoRepository;

    private final CitaRepository citaRepository;


    public DisponibilidadMedicaService(
        DisponibilidadMedicaRepository disponibilidadRepository,
        MedicoRepository medicoRepository,
        CitaRepository citaRepository
    ) {

        this.disponibilidadRepository =
            disponibilidadRepository;

        this.medicoRepository =
            medicoRepository;

        this.citaRepository =
            citaRepository;
    }


    /*
     * ============================================================
     * LISTAR DISPONIBILIDADES DE UN MÉDICO
     * ============================================================
     */
    public List<DisponibilidadMedica> listarPorMedico(
        Long medicoId
    ) {

        if (medicoId == null) {
            return List.of();
        }


        return disponibilidadRepository
            .findByMedicoIdAndActivoTrue(
                medicoId
            );
    }


    /*
     * ============================================================
     * LISTAR DISPONIBILIDADES POR MÉDICO Y FECHA
     * ============================================================
     */
    public List<DisponibilidadMedica> listarPorMedicoYFecha(
        Long medicoId,
        LocalDate fecha
    ) {

        if (
            medicoId == null
            || fecha == null
        ) {

            return List.of();
        }


        return disponibilidadRepository
            .findByMedicoIdAndFechaAndActivoTrue(
                medicoId,
                fecha
            );
    }


    /*
     * ============================================================
     * OBTENER DISPONIBILIDAD
     * ============================================================
     */
    public Optional<DisponibilidadMedica> obtenerPorId(
        Long id
    ) {

        if (id == null) {
            return Optional.empty();
        }


        return disponibilidadRepository
            .findByIdAndActivoTrue(
                id
            );
    }


    /*
     * ============================================================
     * CREAR DISPONIBILIDAD
     * ============================================================
     *
     * Este método será utilizado desde:
     *
     * POST /disponibilidades/crear
     */
    @Transactional
    public String crear(
        Long medicoId,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin
    ) {

        /*
         * Validar datos.
         */
        if (
            medicoId == null
            || fecha == null
            || horaInicio == null
            || horaFin == null
        ) {

            return "Error: faltan datos obligatorios";
        }


        /*
         * No permitimos disponibilidades
         * para fechas anteriores.
         */
        if (
            fecha.isBefore(
                LocalDate.now()
            )
        ) {

            return "Error: no se puede crear disponibilidad en una fecha pasada";
        }


        /*
         * La hora de inicio debe ser menor
         * que la hora final.
         */
        if (
            !horaInicio.isBefore(
                horaFin
            )
        ) {

            return "Error: la hora de inicio debe ser menor que la hora final";
        }


        /*
         * Comprobamos que exista el médico.
         */
        Medico medico =
            medicoRepository
                .findById(
                    medicoId
                )
                .orElse(null);


        if (medico == null) {

            return "Error: el médico no existe";
        }


        /*
         * ========================================================
         * EVITAR DISPONIBILIDADES SOLAPADAS
         * ========================================================
         *
         * Ejemplo:
         *
         * Ya existe:
         * 08:00 - 12:00
         *
         * No permitimos:
         * 09:00 - 11:00
         * 11:00 - 14:00
         *
         * Sí se permitiría:
         * 12:00 - 15:00
         */
        List<DisponibilidadMedica> existentes =
            disponibilidadRepository
                .findByMedicoIdAndFechaAndActivoTrue(
                    medicoId,
                    fecha
                );


        boolean existeSolapamiento =
            existentes
                .stream()
                .anyMatch(
                    disponibilidad -> {

                        LocalTime inicioExistente =
                            disponibilidad.getHoraInicio();

                        LocalTime finExistente =
                            disponibilidad.getHoraFin();


                        return horaInicio.isBefore(finExistente)
                            && horaFin.isAfter(inicioExistente);
                    }
                );


        if (existeSolapamiento) {

            return "Error: el médico ya tiene una disponibilidad que se solapa con ese horario";
        }


        /*
         * Creamos la disponibilidad.
         */
        DisponibilidadMedica disponibilidad =
            new DisponibilidadMedica();


        disponibilidad.setMedico(
            medico
        );


        disponibilidad.setFecha(
            fecha
        );


        disponibilidad.setHoraInicio(
            horaInicio
        );


        disponibilidad.setHoraFin(
            horaFin
        );


        disponibilidad.setActivo(
            true
        );


        disponibilidadRepository.save(
            disponibilidad
        );


        logger.info(
            "Disponibilidad creada. Médico: {}, Fecha: {}, Horario: {} - {}",
            medicoId,
            fecha,
            horaInicio,
            horaFin
        );


        return "Disponibilidad creada correctamente";
    }


    /*
     * ============================================================
     * ACTUALIZAR UNA DISPONIBILIDAD
     * ============================================================
     */
    @Transactional
    public String actualizar(
        Long disponibilidadId,
        LocalTime horaInicio,
        LocalTime horaFin,
        Boolean activo
    ) {

        if (
            disponibilidadId == null
            || horaInicio == null
            || horaFin == null
        ) {

            return "Error: faltan datos obligatorios";
        }


        if (
            !horaInicio.isBefore(
                horaFin
            )
        ) {

            return "Error: la hora de inicio debe ser menor que la hora final";
        }


        Optional<DisponibilidadMedica> dispOpt =
            disponibilidadRepository
                .findById(
                    disponibilidadId
                );


        if (dispOpt.isEmpty()) {

            return "Error: la disponibilidad no existe";
        }


        DisponibilidadMedica disponibilidad =
            dispOpt.get();


        disponibilidad.setHoraInicio(
            horaInicio
        );


        disponibilidad.setHoraFin(
            horaFin
        );


        disponibilidad.setActivo(
            activo != null
                ? activo
                : true
        );


        disponibilidadRepository.save(
            disponibilidad
        );


        logger.info(
            "Disponibilidad {} actualizada",
            disponibilidadId
        );


        return "Disponibilidad actualizada correctamente";
    }


    /*
     * ============================================================
     * ACTUALIZAR DISPONIBILIDADES EN UN RANGO
     * ============================================================
     */
    @Transactional
    public String actualizarRango(
        Long medicoId,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        LocalTime horaInicio,
        LocalTime horaFin,
        Boolean activo
    ) {

        if (
            medicoId == null
            || fechaDesde == null
            || fechaHasta == null
            || horaInicio == null
            || horaFin == null
        ) {

            return "Error: faltan datos obligatorios";
        }


        if (
            !medicoRepository.existsById(
                medicoId
            )
        ) {

            return "Error: el médico no existe";
        }


        if (
            fechaDesde.isAfter(
                fechaHasta
            )
        ) {

            return "Error: la fecha inicial no puede ser posterior a la fecha final";
        }


        if (
            !horaInicio.isBefore(
                horaFin
            )
        ) {

            return "Error: la hora de inicio debe ser menor que la hora final";
        }


        LocalDate fechaActual =
            fechaDesde;


        int actualizadas =
            0;


        while (
            !fechaActual.isAfter(
                fechaHasta
            )
        ) {

            List<DisponibilidadMedica> disponibilidades =
                disponibilidadRepository
                    .findByMedicoIdAndFechaAndActivoTrue(
                        medicoId,
                        fechaActual
                    );


            for (
                DisponibilidadMedica disponibilidad
                    : disponibilidades
            ) {

                disponibilidad.setHoraInicio(
                    horaInicio
                );


                disponibilidad.setHoraFin(
                    horaFin
                );


                disponibilidad.setActivo(
                    activo != null
                        ? activo
                        : true
                );


                disponibilidadRepository.save(
                    disponibilidad
                );


                actualizadas++;
            }


            fechaActual =
                fechaActual.plusDays(1);
        }


        logger.info(
            "Se actualizaron {} disponibilidades del médico {} entre {} y {}",
            actualizadas,
            medicoId,
            fechaDesde,
            fechaHasta
        );


        return "Se actualizaron "
            + actualizadas
            + " espacios de disponibilidad";
    }


    /*
     * ============================================================
     * DESACTIVAR DISPONIBILIDAD
     * ============================================================
     */
    @Transactional
    public String desactivar(
        Long disponibilidadId
    ) {

        Optional<DisponibilidadMedica> dispOpt =
            disponibilidadRepository
                .findById(
                    disponibilidadId
                );


        if (dispOpt.isEmpty()) {

            return "Error: la disponibilidad no existe";
        }


        DisponibilidadMedica disponibilidad =
            dispOpt.get();


        disponibilidad.setActivo(
            false
        );


        disponibilidadRepository.save(
            disponibilidad
        );


        logger.info(
            "Disponibilidad {} desactivada",
            disponibilidadId
        );


        return "Disponibilidad desactivada";
    }


    /*
     * ============================================================
     * ELIMINAR DISPONIBILIDAD
     * ============================================================
     *
     * No eliminamos físicamente una disponibilidad
     * que ya tenga citas asociadas.
     *
     * En ese caso simplemente impedimos la eliminación,
     * porque la cita necesita conservar esa referencia.
     */
    @Transactional
    public String eliminar(
        Long disponibilidadId
    ) {

        if (disponibilidadId == null) {

            return "Error: la disponibilidad no existe";
        }


        Optional<DisponibilidadMedica> dispOpt =
            disponibilidadRepository
                .findById(
                    disponibilidadId
                );


        if (dispOpt.isEmpty()) {

            return "Error: la disponibilidad no existe";
        }


        /*
         * Comprobamos si alguna cita está utilizando
         * esta disponibilidad.
         *
         * Lo hacemos sin necesitar agregar otro método
         * al CitaRepository que ya terminamos anteriormente.
         */
        List<Cita> citas =
            citaRepository.findAll();


        boolean tieneCitas =
            citas
                .stream()
                .anyMatch(
                    cita ->
                        cita.getDisponibilidad() != null
                        &&
                        cita.getDisponibilidad().getId() != null
                        &&
                        cita.getDisponibilidad()
                            .getId()
                            .equals(
                                disponibilidadId
                            )
                );


        if (tieneCitas) {

            return "Error: no se puede eliminar la disponibilidad porque tiene citas asociadas";
        }


        disponibilidadRepository.deleteById(
            disponibilidadId
        );


        logger.info(
            "Disponibilidad {} eliminada",
            disponibilidadId
        );


        return "Disponibilidad eliminada correctamente";
    }
}