package com.example.veterinaria.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.veterinaria.exception.RecursoDuplicadoException;
import com.example.veterinaria.modelo.Rol;
import com.example.veterinaria.modelo.Usuario;
import com.example.veterinaria.repository.RolRepository;
import com.example.veterinaria.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public List<Usuario> findByEstado(String estado) {
        return usuarioRepository.findByEstado(estado);
    }

    public List<Usuario> findByRolNombre(String nombreRol) {
        return usuarioRepository.findByRolNombre(nombreRol);
    }

    public List<Usuario> buscarClientesPorCriterio(String criterio) {
        return usuarioRepository.buscarClientesPorCriterio(criterio);
    }

    public List<Usuario> findVeterinariosActivos() {
        return usuarioRepository.findVeterinariosActivos();
    }

    public List<Usuario> findByDni(String dni) {
        return usuarioRepository.findByDni(dni);
    }

    public Usuario save(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    /**
     * Actualiza los datos de contacto de cualquier usuario.
     *
     * Se carga la entidad existente y se copian sólo los campos editables, de modo que
     * contraseña, dni, especialidad, rol y estado no puedan sobrescribirse con null.
     */
    @Transactional
    public Usuario actualizarPerfil(Long id, Usuario cambios) {
        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        if (cambios.getNombres() != null) {
            existente.setNombres(cambios.getNombres());
        }
        if (cambios.getApellidos() != null) {
            existente.setApellidos(cambios.getApellidos());
        }

        /* La especialidad sólo la maneja un veterinario (o admin) sobre su
           propia ficha. */
        boolean esVeterinario = "VETERINARIO".equals(existente.getRol().getNombre());
        if (cambios.getEspecialidad() != null && (esVeterinario || "ADMIN".equals(cambios.getRol() != null
                ? cambios.getRol().getNombre() : null))) {
            existente.setEspecialidad(cambios.getEspecialidad());
        }

        existente.setCorreo(cambios.getCorreo());
        existente.setTelefono(cambios.getTelefono());
        existente.setDireccion(cambios.getDireccion());

        return usuarioRepository.save(existente);
    }

    /**
     * Crea un usuario validando los datos y codificando la contraseña.
     *
     * @throws IllegalArgumentException si falta un campo obligatorio o la contraseña
     *                                  no cumple las reglas de formato.
     * @throws RecursoDuplicadoException si el usuario o el dni ya existen.
     */
    @Transactional
    public Usuario crearUsuario(Usuario datos) {
        if (datos.getUsername() == null || datos.getUsername().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        if (datos.getNombres() == null || datos.getNombres().isBlank()) {
            throw new IllegalArgumentException("Los nombres son obligatorios");
        }
        if (datos.getApellidos() == null || datos.getApellidos().isBlank()) {
            throw new IllegalArgumentException("Los apellidos son obligatorios");
        }

        String username = datos.getUsername().trim();
        if (usuarioRepository.existsByUsername(username)) {
            throw new RecursoDuplicadoException("El nombre de usuario ya existe");
        }

        validarPassword(datos.getPassword());

        Rol rol = datos.getRol() != null && datos.getRol().getNombre() != null
                ? rolRepository.findByNombre(datos.getRol().getNombre().trim().toUpperCase())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Rol no válido: " + datos.getRol().getNombre()))
                : rolRepository.findByNombre("CLIENTE")
                    .orElseThrow(() -> new IllegalStateException("Rol CLIENTE no encontrado"));

        Usuario nuevo = new Usuario();
        nuevo.setUsername(username);
        nuevo.setPassword(passwordEncoder.encode(datos.getPassword()));
        nuevo.setNombres(datos.getNombres().trim());
        nuevo.setApellidos(datos.getApellidos().trim());
        nuevo.setTelefono(datos.getTelefono());
        nuevo.setDireccion(datos.getDireccion());
        nuevo.setRol(rol);

        /* El correo es opcional: no se sustituye por el nombre de usuario porque
           no tiene por qué ser un correo válido y la columna lleva @Email. */
        nuevo.setCorreo(datos.getCorreo() != null && !datos.getCorreo().isBlank()
                ? datos.getCorreo().trim()
                : null);

        if ("CLIENTE".equals(rol.getNombre())) {
            String dni = datos.getDni();
            if (dni == null || dni.isBlank()) {
                throw new IllegalArgumentException("El rol CLIENTE requiere un DNI");
            }
            dni = dni.trim();
            if (!usuarioRepository.findByDni(dni).isEmpty()) {
                throw new RecursoDuplicadoException("El DNI ya está registrado");
            }
            nuevo.setDni(dni);
        } else if (datos.getDni() != null && !datos.getDni().isBlank()) {
            String dni = datos.getDni().trim();
            if (!usuarioRepository.findByDni(dni).isEmpty()) {
                throw new RecursoDuplicadoException("El DNI ya está registrado");
            }
            nuevo.setDni(dni);
        }

        if ("VETERINARIO".equals(rol.getNombre())) {
            nuevo.setEspecialidad(datos.getEspecialidad());
        }

        nuevo.setEstado("ACTIVO");
        return usuarioRepository.save(nuevo);
    }

    private void validarPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        if (password.length() > 72) {
            throw new IllegalArgumentException("La contraseña no puede superar los 72 caracteres");
        }
    }

    /**
     * Reasigna el rol de un usuario.
     *
     * Con el modelo de tabla única no hay migración de datos: dni y especialidad se
     * conservan. Si el nuevo rol es CLIENTE se exige dni, porque es su campo requerido.
     */
    @Transactional
    public Usuario cambiarRol(Long id, String nombreRol, String dni, String especialidad) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new NoSuchElementException("Rol no encontrado: " + nombreRol));

        if ("CLIENTE".equals(nombreRol)) {
            String dniFinal = (dni != null && !dni.isBlank()) ? dni.trim() : usuario.getDni();
            if (dniFinal == null || dniFinal.isBlank()) {
                throw new IllegalArgumentException("El rol CLIENTE requiere un DNI");
            }
            usuario.setDni(dniFinal);
        }

        if ("VETERINARIO".equals(nombreRol)
                && especialidad != null && !especialidad.isBlank()) {
            usuario.setEspecialidad(especialidad.trim());
        }

        usuario.setRol(rol);
        return usuarioRepository.save(usuario);
    }

    /** Activa o desactiva un usuario sin eliminarlo. */
    @Transactional
    public Usuario cambiarEstado(Long id, String estado) {
        if (!"ACTIVO".equals(estado) && !"INACTIVO".equals(estado)) {
            throw new IllegalArgumentException("Estado no válido: " + estado);
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        usuario.setEstado(estado);
        return usuarioRepository.save(usuario);
    }

    /**
     * Cambia la contraseña del usuario indicado tras verificar la actual.
     *
     * @throws IllegalArgumentException si la contraseña actual no coincide, la nueva
     *                                  no cumple las reglas o es igual a la actual.
     */
    @Transactional
    public Usuario cambiarPassword(String username, String passwordActual,
            String passwordNueva, String passwordConfirmacion) {

        if (passwordActual == null || passwordActual.isBlank()) {
            throw new IllegalArgumentException("Debe introducir su contraseña actual");
        }

        if (passwordNueva == null || passwordNueva.isBlank()) {
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía");
        }

        if (passwordNueva.length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres");
        }

        if (passwordNueva.length() > 72) {
            throw new IllegalArgumentException("La nueva contraseña no puede superar los 72 caracteres");
        }

        if (!passwordNueva.equals(passwordConfirmacion)) {
            throw new IllegalArgumentException("Las contraseñas nuevas no coinciden");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(passwordActual, usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        if (passwordEncoder.matches(passwordNueva, usuario.getPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser distinta de la actual");
        }

        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        return usuarioRepository.save(usuario);
    }
}
