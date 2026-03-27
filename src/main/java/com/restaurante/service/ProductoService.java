package com.restaurante.service;

import com.restaurante.entity.*;
import com.restaurante.exception.*;
import com.restaurante.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> listarActivos() {
        return productoRepository.findAllActivos();
    }

    public List<Producto> listarPorCategoria(Integer categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId);
    }

    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
    }

    @Transactional
    public Producto crear(Producto producto, Integer categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", categoriaId));
        producto.setCategoria(categoria);
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaActualizacion(LocalDateTime.now());
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(Long id, Producto datos, Integer categoriaId) {
        Producto producto = buscarPorId(id);
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", categoriaId));

        producto.setNombre(datos.getNombre());
        producto.setDescripcion(datos.getDescripcion());
        producto.setPrecio(datos.getPrecio());
        producto.setCategoria(categoria);
        producto.setTiempoPreparacion(datos.getTiempoPreparacion());
        producto.setEsPreparado(datos.getEsPreparado());
        producto.setFechaActualizacion(LocalDateTime.now());
        if (datos.getImagenUrl() != null) producto.setImagenUrl(datos.getImagenUrl());

        return productoRepository.save(producto);
    }

    @Transactional
    public Producto cambiarEstado(Long id, Producto.EstadoProducto estado) {
        Producto producto = buscarPorId(id);
        producto.setEstado(estado);
        producto.setFechaActualizacion(LocalDateTime.now());
        return productoRepository.save(producto);
    }

    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new com.restaurante.exception.ResourceNotFoundException("Producto", id));
        productoRepository.delete(producto);
    }

}