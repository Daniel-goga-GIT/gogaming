package com.gogaming.services;

import com.gogaming.controllers.UserController;
import com.gogaming.dtos.UserCreateDTO;
import com.gogaming.dtos.UserDTO;
import com.gogaming.entities.Role;
import com.gogaming.entities.User;
import com.gogaming.mappers.UserMapper;
import com.gogaming.repositories.UserRepository;
import com.gogaming.repositories.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    //Busca el usuario y busca el id
    public Long getIdByEmail(String email) {
        return userRepository.getIdByEmail(email);
    }

    public UserDTO getUserDTOById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);

        if(userOptional.isPresent()) {
            return userMapper.toDTO(userOptional.get());
        }
        throw new RuntimeException("User con identificado" + id +"encontrado");
    }

    public User getUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);

        if(userOpt.isPresent()) {
            return userOpt.get();
        }
        throw new RuntimeException("User con identificado" + id +"encontrado");
    }

    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        // verificar si ya existe un usuario con el mismo nombre
        if (userRepository.existsByEmail(userCreateDTO.getEmail())) {
            throw new IllegalArgumentException("Este usuario ya existe");
        }

        User user = userMapper.toEntity(userCreateDTO);

        // 1. Hashear contraseña
        user.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));

        String fileName = null;
        if (userCreateDTO.getImageFile() != null && !userCreateDTO.getImageFile().isEmpty()){
            try {
                fileName = fileStorageService.saveFile(userCreateDTO.getImageFile());
            } catch (java.io.IOException e) {
                throw new RuntimeException("Error al cargar imagen", e);
            }
        }
        user.setImage(fileName);

        // 2. Asignamos el Rol por defecto
        // Buscamos el rol ROLE_USER en la BD
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: El rol ROLE_USER no existe en la base de datos."));

        // Inicializamos el Set y añadimos el rol
        user.setRoles(Collections.singleton(userRole));

        // 3. Guardar y mapear a DTO
        User savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);
    }

    //IMAGENES
    public UserDTO uploadImage(Long id, org.springframework.web.multipart.MultipartFile file) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // Primero se guarda la nueva (si no es válida, el usuario conserva la anterior)...
        String anterior = user.getImage();
        String fileName;
        try {
            fileName = fileStorageService.saveFile(file);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error al guardar la imagen en el servidor", e);
        }
        user.setImage(fileName);
        User savedUser = userRepository.save(user);

        // ...y después se borra la antigua.
        if (anterior != null && !anterior.isEmpty()) {
            fileStorageService.deleteFile(anterior);
        }
        return userMapper.toDTO(savedUser);
    }


    //TWO FACTOR
    public void saveCode(String code, Long id){
        if (id != null) {
            Optional<User> userOptional = userRepository.findById(id);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setCode(code);
                userRepository.save(user);
            }
        }
    }

    public void clear2FactorCode(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setCode(null);
            userRepository.save(user);
        });
    }

    public User findOrCreateGoogleUser(String email, String firstName, String lastName) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: El rol ROLE_USER no existe en la base de datos."));

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setEnabled(true);
        newUser.setFirstName((firstName == null || firstName.isBlank()) ? "Google" : firstName);
        newUser.setLastName((lastName == null || lastName.isBlank()) ? "User" : lastName);

        // los usuarios de Google no se autentican con contrasena en este flujo, pero el campo es obligatorio en la BD
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        newUser.setRoles(Collections.singleton(userRole));

        return userRepository.save(newUser);
    }


}
