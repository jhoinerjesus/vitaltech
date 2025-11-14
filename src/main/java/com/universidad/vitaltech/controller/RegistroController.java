package com.universidad.vitaltech.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.universidad.vitaltech.model.Rol;
import com.universidad.vitaltech.model.Usuario;
import com.universidad.vitaltech.repository.UsuarioRepository;

@Controller
public class RegistroController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/registro/paciente")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro-paciente"; 
    }

    @PostMapping("/registro/paciente")
    public String registrarPaciente(Usuario usuario, Model model) {
        // Validar si existe
        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            model.addAttribute("error", "Ya existe un usuario con ese correo electrónico.");
            return "registro-paciente";
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRol(Rol.PACIENTE);
        usuario.setActivo(true);

        usuarioRepository.save(usuario);

        model.addAttribute("mensaje", "Registro exitoso. ¡Ya puedes iniciar sesión!");
        return "login"; 
    }
}
