package org.iesalixar.daw2.alvarolopez.axisgarage.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.VehicleCategoryDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.services.VehicleCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Vehicle Categories", description = "Gestión de categorías de vehículos")
public class VehicleCategoryController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleCategoryController.class);

    @Autowired
    private VehicleCategoryService categoryService;

    @Operation(summary = "Listar todas las categorías")
    @GetMapping
    public ResponseEntity<List<VehicleCategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Obtener categoría por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        Optional<VehicleCategoryDTO> dto = categoryService.getCategoryById(id);
        if (dto.isPresent()) {
            return ResponseEntity.ok(dto.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Categoría no encontrada.");
    }

    @Operation(summary = "Crear una nueva categoría")
    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody VehicleCategoryDTO dto) {
        try {
            VehicleCategoryDTO created = categoryService.createCategory(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Actualizar una categoría existente")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @Valid @RequestBody VehicleCategoryDTO dto) {
        try {
            VehicleCategoryDTO updated = categoryService.updateCategory(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Eliminar una categoría")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok("Categoría eliminada con éxito.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
