package com.Proyecto.Hospital.controller;

import com.Proyecto.Hospital.Model.DisponibilidadMedica;
import com.Proyecto.Hospital.Model.Usuario;

import com.Proyecto.Hospital.Repository.UsuarioRepository;

import com.Proyecto.Hospital.Service.DisponibilidadMedicaService;
import com.Proyecto.Hospital.Service.MedicoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

import java.time.LocalDate;
import java.time.LocalTime;

import java.util.List;

@Controller
public class DisponibilidadController {

    private static final Logger logger =
        LoggerFactory.getLogger(
            DisponibilidadController.class
        );


    private final DisponibilidadMedicaService disponibilidadService;

    private final MedicoService medicoService;

    private final UsuarioRepository usuarioRepository;


    public DisponibilidadController(
        DisponibilidadMedicaService disponibilidadService,
        MedicoService medicoService,
        UsuarioRepository usuarioRepository
    ) {

        this.disponibilidadService =
            disponibilidadService;

        this.medicoService =
            medicoService;

        this.usuarioRepository =
            usuarioRepository;
    }


    /*
     * ============================================================
     * CREAR DISPONIBILIDAD
     * ============================================================
     *
     * Esta ruta es utilizada desde formularioMedico.html.
     *
     * El administrador selecciona:
     *
     * - médico
     * - fecha
     * - hora inicial
     * - hora final
     *
     * y se crea un nuevo espacio disponible.
     */
    @PostMapping("/disponibilidades/crear")
    public String crearDisponibilidad(
        @RequestParam Long medicoId,
        @RequestParam String fechaStr,
        @RequestParam String horaInicioStr,
        @RequestParam String horaFinStr,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {

        Usuario usuario =
            obtenerUsuarioAutenticado(
                principal
            );


        /*
         * Aunque SecurityConfig también protege esta ruta,
         * hacemos una segunda validación desde el controlador.
         */
        if (
            usuario == null
            || !"ADMIN".equalsIgnoreCase(
                usuario.getRol()
            )
        ) {

            return "redirect:/acceso-denegado";
        }


        try {

            /*
             * Convertimos los valores recibidos
             * desde los inputs HTML.
             */
            LocalDate fecha =
                LocalDate.parse(
                    fechaStr
                );


            LocalTime horaInicio =
                LocalTime.parse(
                    horaInicioStr
                );


            LocalTime horaFin =
                LocalTime.parse(
                    horaFinStr
                );


            /*
             * El servicio realiza las validaciones importantes:
             *
             * - médico existente
             * - fecha no pasada
             * - inicio < fin
             * - evitar solapamientos
             */
            String mensaje =
                disponibilidadService.crear(
                    medicoId,
                    fecha,
                    horaInicio,
                    horaFin
                );


            if (
                "Disponibilidad creada correctamente"
                    .equals(mensaje)
            ) {

                redirectAttributes.addFlashAttribute(
                    "success",
                    mensaje
                );


                logger.info(
                    "Administrador {} creó disponibilidad para médico {}: {} {} - {}",
                    usuario.getEmail(),
                    medicoId,
                    fecha,
                    horaInicio,
                    horaFin
                );

            } else {

                redirectAttributes.addFlashAttribute(
                    "error",
                    mensaje
                );
            }


        } catch (Exception e) {

            logger.error(
                "Error al crear disponibilidad para médico {}",
                medicoId,
                e
            );


            redirectAttributes.addFlashAttribute(
                "error",
                "Error al crear la disponibilidad: "
                    + e.getMessage()
            );
        }


        /*
         * Regresamos al formulario del mismo médico.
         */
        return "redirect:/editarMedico?id="
            + medicoId;
    }


    /*
     * ============================================================
     * ELIMINAR DISPONIBILIDAD
     * ============================================================
     *
     * Utilizamos POST porque esta operación modifica
     * información del sistema.
     *
     * No usamos GET para eliminar.
     */
    @PostMapping("/disponibilidades/eliminar/{id}")
    public String eliminarDisponibilidad(
        @PathVariable Long id,
        @RequestParam Long medicoId,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {

        Usuario usuario =
            obtenerUsuarioAutenticado(
                principal
            );


        if (
            usuario == null
            || !"ADMIN".equalsIgnoreCase(
                usuario.getRol()
            )
        ) {

            return "redirect:/acceso-denegado";
        }


        try {

            String mensaje =
                disponibilidadService.eliminar(
                    id
                );


            if (
                "Disponibilidad eliminada correctamente"
                    .equals(mensaje)
            ) {

                redirectAttributes.addFlashAttribute(
                    "success",
                    mensaje
                );


                logger.info(
                    "Administrador {} eliminó disponibilidad {}",
                    usuario.getEmail(),
                    id
                );

            } else {

                redirectAttributes.addFlashAttribute(
                    "error",
                    mensaje
                );
            }


        } catch (Exception e) {

            logger.error(
                "Error al eliminar disponibilidad {}",
                id,
                e
            );


            redirectAttributes.addFlashAttribute(
                "error",
                "Error al eliminar la disponibilidad: "
                    + e.getMessage()
            );
        }


        return "redirect:/editarMedico?id="
            + medicoId;
    }


    /*
     * ============================================================
     * PANTALLA DE EDICIÓN DE DISPONIBILIDADES
     * ============================================================
     */
    @GetMapping("/disponibilidades/editar")
    public String editarDisponibilidades(
        @RequestParam(required = false) Long medicoId,
        @RequestParam(required = false) String fecha,
        Model model,
        Principal principal
    ) {

        Usuario usuario =
            obtenerUsuarioAutenticado(
                principal
            );


        if (
            usuario == null
            || !"ADMIN".equalsIgnoreCase(
                usuario.getRol()
            )
        ) {

            return "redirect:/acceso-denegado";
        }


        /*
         * Cargamos médicos para el selector.
         */
        model.addAttribute(
            "medicos",
            medicoService.ListarMedicos()
        );


        /*
         * Si se seleccionó médico y fecha,
         * cargamos sus disponibilidades.
         */
        if (
            medicoId != null
            && fecha != null
            && !fecha.isBlank()
        ) {

            try {

                LocalDate fechaObj =
                    LocalDate.parse(
                        fecha
                    );


                List<DisponibilidadMedica> disponibilidades =
                    disponibilidadService
                        .listarPorMedicoYFecha(
                            medicoId,
                            fechaObj
                        );


                model.addAttribute(
                    "disponibilidades",
                    disponibilidades
                );


                model.addAttribute(
                    "medicoSeleccionado",
                    medicoId
                );


                model.addAttribute(
                    "fechaSeleccionada",
                    fecha
                );


            } catch (Exception e) {

                logger.error(
                    "Error al cargar disponibilidades",
                    e
                );


                model.addAttribute(
                    "error",
                    "Fecha inválida"
                );
            }
        }


        return "editarDisponibilidad";
    }


    /*
     * ============================================================
     * ACTUALIZAR UNA DISPONIBILIDAD
     * ============================================================
     */
    @PostMapping("/disponibilidades/actualizar/{id}")
    public String actualizarDisponibilidad(
        @PathVariable Long id,
        @RequestParam LocalTime horaInicio,
        @RequestParam LocalTime horaFin,
        @RequestParam(required = false) Boolean activo,
        Model model,
        Principal principal
    ) {

        Usuario usuario =
            obtenerUsuarioAutenticado(
                principal
            );


        if (
            usuario == null
            || !"ADMIN".equalsIgnoreCase(
                usuario.getRol()
            )
        ) {

            return "redirect:/acceso-denegado";
        }


        try {

            String mensaje =
                disponibilidadService.actualizar(
                    id,
                    horaInicio,
                    horaFin,
                    activo
                );


            if (
                !"Disponibilidad actualizada correctamente"
                    .equals(mensaje)
            ) {

                model.addAttribute(
                    "error",
                    mensaje
                );


                model.addAttribute(
                    "medicos",
                    medicoService.ListarMedicos()
                );


                return "editarDisponibilidad";
            }


            logger.info(
                "Disponibilidad {} actualizada por administrador {}",
                id,
                usuario.getEmail()
            );


            return "redirect:/disponibilidades/editar";


        } catch (Exception e) {

            logger.error(
                "Error al actualizar disponibilidad {}",
                id,
                e
            );


            model.addAttribute(
                "error",
                "Error: "
                    + e.getMessage()
            );


            model.addAttribute(
                "medicos",
                medicoService.ListarMedicos()
            );


            return "editarDisponibilidad";
        }
    }


    /*
     * ============================================================
     * ACTUALIZAR DISPONIBILIDADES EN RANGO
     * ============================================================
     */
    @PostMapping("/disponibilidades/actualizar-rango")
    public String actualizarDisponibilidadRango(
        @RequestParam Long medicoId,
        @RequestParam String fechaDesde,
        @RequestParam String fechaHasta,
        @RequestParam LocalTime horaInicio,
        @RequestParam LocalTime horaFin,
        @RequestParam(required = false) Boolean activo,
        Model model,
        Principal principal
    ) {

        Usuario usuario =
            obtenerUsuarioAutenticado(
                principal
            );


        if (
            usuario == null
            || !"ADMIN".equalsIgnoreCase(
                usuario.getRol()
            )
        ) {

            return "redirect:/acceso-denegado";
        }


        try {

            LocalDate desde =
                LocalDate.parse(
                    fechaDesde
                );


            LocalDate hasta =
                LocalDate.parse(
                    fechaHasta
                );


            String mensaje =
                disponibilidadService
                    .actualizarRango(
                        medicoId,
                        desde,
                        hasta,
                        horaInicio,
                        horaFin,
                        activo
                    );


            /*
             * Si ocurrió alguna validación,
             * mostramos el error.
             */
            if (
                mensaje.startsWith(
                    "Error:"
                )
            ) {

                model.addAttribute(
                    "error",
                    mensaje
                );


                model.addAttribute(
                    "medicos",
                    medicoService.ListarMedicos()
                );


                return "editarDisponibilidad";
            }


            logger.info(
                "Disponibilidades del médico {} actualizadas entre {} y {} por administrador {}",
                medicoId,
                desde,
                hasta,
                usuario.getEmail()
            );


            return "redirect:/disponibilidades/editar";


        } catch (Exception e) {

            logger.error(
                "Error al actualizar disponibilidades en rango",
                e
            );


            model.addAttribute(
                "error",
                "Error: "
                    + e.getMessage()
            );


            model.addAttribute(
                "medicos",
                medicoService.ListarMedicos()
            );


            return "editarDisponibilidad";
        }
    }


    /*
     * ============================================================
     * DESACTIVAR DISPONIBILIDAD
     * ============================================================
     */
    @PostMapping("/disponibilidades/desactivar/{id}")
    public String desactivarDisponibilidad(
        @PathVariable Long id,
        @RequestParam(required = false) Long medicoId,
        Principal principal,
        RedirectAttributes redirectAttributes
    ) {

        Usuario usuario =
            obtenerUsuarioAutenticado(
                principal
            );


        if (
            usuario == null
            || !"ADMIN".equalsIgnoreCase(
                usuario.getRol()
            )
        ) {

            return "redirect:/acceso-denegado";
        }


        try {

            String mensaje =
                disponibilidadService.desactivar(
                    id
                );


            if (
                mensaje.startsWith(
                    "Error:"
                )
            ) {

                redirectAttributes.addFlashAttribute(
                    "error",
                    mensaje
                );

            } else {

                redirectAttributes.addFlashAttribute(
                    "success",
                    mensaje
                );
            }


        } catch (Exception e) {

            logger.error(
                "Error al desactivar disponibilidad {}",
                id,
                e
            );


            redirectAttributes.addFlashAttribute(
                "error",
                "Error al desactivar la disponibilidad: "
                    + e.getMessage()
            );
        }


        if (medicoId != null) {

            return "redirect:/editarMedico?id="
                + medicoId;
        }


        return "redirect:/disponibilidades/editar";
    }


    /*
     * ============================================================
     * OBTENER USUARIO AUTENTICADO
     * ============================================================
     */
    private Usuario obtenerUsuarioAutenticado(
        Principal principal
    ) {

        if (principal == null) {

            return null;
        }


        return usuarioRepository
            .findByEmail(
                principal.getName()
            )
            .orElse(null);
    }
}