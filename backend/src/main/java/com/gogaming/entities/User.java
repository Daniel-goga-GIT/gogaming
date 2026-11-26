package com.gogaming.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idusuario")
    private Long id;

    @Column(name = "correo_electronico", nullable = false, unique = true)
    private String email;

    /** Código 2FA pendiente. Fuera del toString para que no acabe en los logs. */
    @Column(name = "code")
    @lombok.ToString.Exclude
    private String code;

    @Column(name = "image")
    private String image;

    @Column(name = "contrasena", nullable = false)
    @lombok.ToString.Exclude
    private String password;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "nombre", nullable = false)
    private String firstName;

    @Column(name = "apellidos", nullable = false)
    private String lastName;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
    
    @Column(name = "last_password_change_date")
    private LocalDateTime lastPasswordChangeDate;


    // Relación muchos a muchos con la entidad `Role`.
    // Se establece FetchType.EAGER para que se carguen los roles junto al usuario
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;


    public User(String email, String firstName, String lastName, String password) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
    }
}
