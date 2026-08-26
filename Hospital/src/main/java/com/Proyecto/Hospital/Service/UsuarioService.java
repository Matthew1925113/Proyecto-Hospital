package com.Proyecto.Hospital.Service;

import com.Proyecto.Hospital.Model.Usuario;
import com.Proyecto.Hospital.Repository.MedicoRepository;
import com.Proyecto.Hospital.Repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final MedicoRepository medicoRepository;
    private final PasswordEncoder passwordEncoder;


    public UsuarioService(
        UsuarioRepository repository,
        MedicoRepository medicoRepository,
        PasswordEncoder passwordEncoder
    ) {

        this.repository = repository;
        this.medicoRepository = medicoRepository;
        this.passwordEncoder = passwordEncoder;
    }


    /*
     * ============================================================
     * LISTAR USUARIOS
     * ============================================================
     */
    public List<Usuario> ListarUsuarios() {

        return repository.findAll();
    }


    /*
     * ============================================================
     * OBTENER USUARIO
     * ============================================================
     */
    public Usuario ObtenerUsuario(Long id) {

        return repository
            .findById(id)
            .orElse(null);
    }


    /*
     * ============================================================
     * GUARDAR USUARIO
     * ============================================================
     */
    public String GuardarUsuario(Usuario usuario) {

        /*
         * --------------------------------------------------------
         * VALIDACIONES BÁSICAS
         * --------------------------------------------------------
         */
        if (usuario == null) {

            return "Error: los datos del usuario son inválidos";
        }


        if (
            usuario.getNombre() == null
            || usuario.getNombre().isBlank()
        ) {

            return "El nombre es obligatorio";
        }


        if (
            usuario.getEmail() == null
            || usuario.getEmail().isBlank()
        ) {

            return "El email es obligatorio";
        }


        if (
            usuario.getRol() == null
            || usuario.getRol().isBlank()
        ) {

            return "El rol es obligatorio";
        }


        /*
         * Normalizamos el correo.
         */
        usuario.setEmail(
            usuario.getEmail()
                .trim()
                .toLowerCase()
        );


        /*
         * Normalizamos el rol.
         */
        usuario.setRol(
            usuario.getRol()
                .trim()
                .toUpperCase()
        );


        /*
         * --------------------------------------------------------
         * VALIDAR ROLES PERMITIDOS
         * --------------------------------------------------------
         *
         * Evitamos que desde una petición manipulada
         * puedan guardarse roles no reconocidos.
         */
        if (
            !"ADMIN".equals(usuario.getRol())
            && !"USUARIO".equals(usuario.getRol())
            && !"MEDICO".equals(usuario.getRol())
        ) {

            return "El rol seleccionado no es válido";
        }


        /*
         * --------------------------------------------------------
         * VALIDAR CUENTA MEDICO
         * --------------------------------------------------------
         *
         * Si el administrador crea una cuenta con rol MEDICO,
         * debe existir previamente un médico registrado
         * con exactamente ese correo electrónico.
         *
         * Ejemplo:
         *
         * Médico:
         * dr.carlos@hospital.com
         *
         * Usuario:
         * dr.carlos@hospital.com
         * Rol: MEDICO
         *
         * Esto permite saber qué médico inició sesión.
         */
        if (
            "MEDICO".equals(
                usuario.getRol()
            )
        ) {

            boolean medicoExiste =
                medicoRepository
                    .findByEmail(
                        usuario.getEmail()
                    )
                    .isPresent();


            if (!medicoExiste) {

                return "No existe un médico registrado con ese email. "
                    + "Primero debes registrar al médico y utilizar el mismo correo "
                    + "para crear su cuenta con rol MEDICO";
            }
        }


        /*
         * --------------------------------------------------------
         * VALIDAR EMAIL DUPLICADO
         * --------------------------------------------------------
         */
        if (
            usuario.getId() == null
        ) {

            /*
             * Usuario nuevo.
             */
            if (
                repository
                    .findByEmail(
                        usuario.getEmail()
                    )
                    .isPresent()
            ) {

                return "El email ya está en uso";
            }

        } else {

            /*
             * Usuario existente.
             *
             * Permitimos conservar su propio email,
             * pero no utilizar el de otra cuenta.
             */
            Usuario existente =
                repository
                    .findByEmail(
                        usuario.getEmail()
                    )
                    .orElse(null);


            if (
                existente != null
                && !existente
                    .getId()
                    .equals(
                        usuario.getId()
                    )
            ) {

                return "El email ya está en uso";
            }
        }


        /*
         * --------------------------------------------------------
         * MANEJAR CONTRASEÑA
         * --------------------------------------------------------
         */
        if (
            usuario.getId() == null
        ) {

            /*
             * Para un usuario nuevo,
             * la contraseña es obligatoria.
             */
            if (
                usuario.getPassword() == null
                || usuario.getPassword().isBlank()
            ) {

                return "La contraseña es obligatoria";
            }


            /*
             * Codificamos la contraseña.
             */
            usuario.setPassword(
                passwordEncoder.encode(
                    usuario.getPassword()
                )
            );

        } else {

            /*
             * Estamos editando un usuario existente.
             */
            Usuario existente =
                ObtenerUsuario(
                    usuario.getId()
                );


            if (existente == null) {

                return "El usuario no existe";
            }


            /*
             * Si no escribimos una nueva contraseña,
             * conservamos la contraseña actual.
             */
            if (
                usuario.getPassword() == null
                || usuario.getPassword().isBlank()
                || usuario
                    .getPassword()
                    .startsWith("$2")
            ) {

                usuario.setPassword(
                    existente.getPassword()
                );

            } else {

                /*
                 * Si se escribió una nueva contraseña,
                 * la codificamos.
                 */
                usuario.setPassword(
                    passwordEncoder.encode(
                        usuario.getPassword()
                    )
                );
            }
        }


        /*
         * --------------------------------------------------------
         * GUARDAR
         * --------------------------------------------------------
         */
        repository.save(
            usuario
        );


        return "El usuario ha sido guardado correctamente";
    }


    /*
     * ============================================================
     * ELIMINAR USUARIO
     * ============================================================
     */
    public void EliminarUsuario(Long id) {

        repository.deleteById(
            id
        );
    }
}