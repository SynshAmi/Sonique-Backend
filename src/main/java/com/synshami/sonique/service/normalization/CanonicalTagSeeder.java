package com.synshami.sonique.service.normalization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synshami.sonique.dto.ontology.Ontology;
import com.synshami.sonique.dto.ontology.OntologyNode;
import com.synshami.sonique.entity.CanonicalTag;
import com.synshami.sonique.enums.CanonicalTagCategory;
import com.synshami.sonique.exception.ResourceNotFoundException;
import com.synshami.sonique.repository.CanonicalTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class CanonicalTagSeeder implements CommandLineRunner {

    private final CanonicalTagRepository canonicalTagRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {

        if (canonicalTagRepository.count() > 0) {
            return;
        }

        InputStream inputStream=getClass().getClassLoader().getResourceAsStream("ontology.json");
        if (inputStream==null) {
            throw new ResourceNotFoundException("Ontology file not found");
        }

        Ontology ontology = objectMapper.readValue(inputStream, Ontology.class);

        for (OntologyNode genre : ontology.getGenres()) {
            saveNode(genre, null, CanonicalTagCategory.GENRE);
        }

        for (String trait : ontology.getMusicalTraits()) {
            canonicalTagRepository.save(
                    CanonicalTag.builder()
                            .name(trait)
                            .category(CanonicalTagCategory.MUSICAL_TRAIT)
                            .build()
            );
        }

        for (String characteristic : ontology.getVocalCharacteristics()) {
            canonicalTagRepository.save(
                    CanonicalTag.builder()
                            .name(characteristic)
                            .category(CanonicalTagCategory.VOCAL_CHARACTERISTIC)
                            .build()
            );
        }

    }

    private CanonicalTag saveNode(
            OntologyNode node,
            CanonicalTag parent,
            CanonicalTagCategory category
    ) {

        CanonicalTag canonicalTag = CanonicalTag.builder()
                .name(node.getName())
                .category(category)
                .parent(parent)
                .build();

        CanonicalTag savedTag = canonicalTagRepository.save(canonicalTag);

        if (node.getChildren() != null) {
            for (OntologyNode child : node.getChildren()) {
                saveNode(child, savedTag, category);
            }
        }

        return savedTag;
    }
}