package org.iesalixar.daw2.alvarolopez.axisgarage.services;

import org.iesalixar.daw2.alvarolopez.axisgarage.dtos.VehicleCategoryDTO;
import org.iesalixar.daw2.alvarolopez.axisgarage.entities.VehicleCategory;
import org.iesalixar.daw2.alvarolopez.axisgarage.mappers.VehicleCategoryMapper;
import org.iesalixar.daw2.alvarolopez.axisgarage.repositories.VehicleCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VehicleCategoryService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleCategoryService.class);

    @Autowired
    private VehicleCategoryRepository categoryRepository;

    @Autowired
    private VehicleCategoryMapper categoryMapper;

    public List<VehicleCategoryDTO> getAllCategories() {
        logger.info("Listando todas las categorías de vehículos");
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<VehicleCategoryDTO> getCategoryById(Long id) {
        return categoryRepository.findById(id).map(categoryMapper::toDTO);
    }

    public VehicleCategoryDTO createCategory(VehicleCategoryDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        }
        VehicleCategory entity = categoryMapper.toEntity(dto);
        VehicleCategory saved = categoryRepository.save(entity);
        logger.info("Categoría creada con ID {}", saved.getId());
        return categoryMapper.toDTO(saved);
    }

    public VehicleCategoryDTO updateCategory(Long id, VehicleCategoryDTO dto) {
        VehicleCategory existing = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));

        if (categoryRepository.existsByNameAndIdNot(dto.getName(), id)) {
            throw new IllegalArgumentException("Ya existe otra categoría con ese nombre.");
        }

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());

        VehicleCategory saved = categoryRepository.save(existing);
        logger.info("Categoría actualizada con ID {}", saved.getId());
        return categoryMapper.toDTO(saved);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Categoría no encontrada con ID: " + id);
        }
        categoryRepository.deleteById(id);
        logger.info("Categoría eliminada con ID {}", id);
    }
}
