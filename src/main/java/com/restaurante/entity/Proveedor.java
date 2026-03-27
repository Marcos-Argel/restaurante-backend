package com.restaurante.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "proveedores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "razon_social", length = 200)
    private String razonSocial;

    @Column(name = "ruc_nit", length = 50)
    private String rucNit;

    @Column(length = 50)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(length = 255)
    private String direccion;

    @Column(name = "contacto_nombre", length = 100)
    private String contactoNombre;

    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
