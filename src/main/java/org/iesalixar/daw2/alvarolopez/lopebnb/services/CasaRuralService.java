package org.iesalixar.daw2.alvarolopez.lopebnb.services;

import org.iesalixar.daw2.alvarolopez.lopebnb.dtos.CasaRuralDTO;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.CasaRural;
import org.iesalixar.daw2.alvarolopez.lopebnb.entities.Propietario;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.CasaRuralRepository;
import org.iesalixar.daw2.alvarolopez.lopebnb.repositories.PropietarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service // ¡OBLIGATORIO para que Spring lo detecte!
public class CasaRuralService {

    @Autowired
    private CasaRuralRepository casaRuralRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    // --- 1. LISTAR CON PAGINACIÓN ---
    public Page<CasaRuralDTO> getAllCasasRurales(Pageable pageable) {
        Page<CasaRural> casaRuralPage = casaRuralRepository.findAll(pageable);
        return casaRuralPage.map(this::toDTO);
    }

    // --- 2. OBTENER POR ID ---
    public CasaRuralDTO getCasaRuralById(Long id) {
        CasaRural casaRural = casaRuralRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Error: No se encontró la casa rural con ID: " + id));
        return toDTO(casaRural);
    }

    // crear
    public CasaRuralDTO createCasaRural(CasaRuralDTO dto) {
        if ()
    }


}