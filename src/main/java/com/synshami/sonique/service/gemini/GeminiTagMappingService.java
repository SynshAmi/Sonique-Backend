package com.synshami.sonique.service.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synshami.sonique.dto.gemini.*;
import com.synshami.sonique.entity.CanonicalTag;
import com.synshami.sonique.entity.Tag;
import com.synshami.sonique.entity.TagMapping;
import com.synshami.sonique.enums.CanonicalTagCategory;
import com.synshami.sonique.exception.GeminiResponseParsingException;
import com.synshami.sonique.repository.CanonicalTagRepository;
import com.synshami.sonique.repository.TagMappingRepository;
import com.synshami.sonique.repository.TagRepository;
import com.synshami.sonique.service.normalization.TagNormalizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiTagMappingService {

    private final GeminiService geminiService;
    private final TagRepository tagRepository;
    private final TagMappingRepository tagMappingRepository;
    private final CanonicalTagRepository canonicalTagRepository;
    private final TagNormalizationService tagNormalizationService;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void generateMappings() {

        List<Tag> unmappedTags = tagRepository.findUnmappedTags(PageRequest.of(0, 30));

        if (unmappedTags.isEmpty())
            return;

        List<CanonicalTag> canonicalTags = canonicalTagRepository.findAll();

        String prompt = buildPrompt(unmappedTags, canonicalTags);

        GenerateContentRequest request = new GenerateContentRequest();

        GenerationConfig generationConfig = new GenerationConfig();
        generationConfig.setResponseMimeType("application/json");

        request.setGenerationConfig(generationConfig);

        Content content = new Content();
        content.setRole("user");

        Part part = new Part();
        part.setText(prompt);

        content.setParts(List.of(part));

        request.setContents(List.of(content));

        GenerateContentResponse response = geminiService.generateContent(request);

        if (response == null
                || response.getCandidates() == null
                || response.getCandidates().isEmpty()) {
            return;
        }

        Candidate candidate = response.getCandidates().get(0);

        if (candidate.getContent() == null
                || candidate.getContent().getParts() == null
                || candidate.getContent().getParts().isEmpty()) {
            return;
        }

        String responseJson = candidate.getContent()
                .getParts()
                .get(0)
                .getText();

        List<TagMappingResponse> mappings;

        try {
            mappings = objectMapper.readValue(responseJson, new TypeReference<List<TagMappingResponse>>() {
            });
        } catch (JsonProcessingException e) {
            throw new GeminiResponseParsingException("Failed to parse Gemini response", e);
        }

        Map<String, Tag> rawTagMap = new HashMap<>();
        for (Tag tag : unmappedTags) {
            rawTagMap.put(tag.getName(), tag);
        }

        Map<String, CanonicalTag> canonicalTagMap = new HashMap<>();

        for (CanonicalTag tag : canonicalTags) {
            canonicalTagMap.put(tag.getName(), tag);
        }

        List<TagMapping> tagMappings = new ArrayList<>();

        for (TagMappingResponse mapping : mappings) {

            Tag rawTag = rawTagMap.get(mapping.getRawTag());
            CanonicalTag canonicalTag = canonicalTagMap.get(mapping.getCanonicalTag());

            if (rawTag != null && canonicalTag != null) {

                tagMappings.add(
                        TagMapping.builder()
                                .rawTag(rawTag)
                                .canonicalTag(canonicalTag)
                                .build()
                );

            }
        }

        if (tagMappings.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
                """
                INSERT INTO tag_mappings (raw_tag_id, canonical_tag_id)
                VALUES (?, ?)
                """,
                tagMappings,
                tagMappings.size(),
                (ps, mapping) -> {
                    ps.setLong(1, mapping.getRawTag().getId());
                    ps.setLong(2, mapping.getCanonicalTag().getId());
                }
        );

        tagNormalizationService.reloadCache();
    }

    private String buildPrompt(List<Tag> unmappedTags, List<CanonicalTag> canonicalTags) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("""
            You are an expert music metadata classification system.

            Your task is to classify raw Last.fm music tags into the canonical music ontology of a music personality and compatibility application.

            Select the single best matching canonical tag from the provided ontology for every raw Last.fm tag.

            ======================================
            ONTOLOGY
            ======================================

            """);

        appendOntology(prompt, canonicalTags);

        prompt.append("""

            ======================================
            RULES
            ======================================

            1. Use ONLY the canonical tags provided in the ontology.
            2. Never invent new canonical tags.
            3. Every raw tag MUST map to exactly one canonical tag.
            4. Do NOT omit any input tag.
            5. Choose the closest semantic match.
            6. If a raw tag represents a broad musical category and there is no evidence that it belongs to a more specific subgenre, map it to the parent genre instead of guessing a child genre.
            7. If a tag is not useful for describing a user's musical taste, map it to "Ignore".

            Tags that should generally map to "Ignore" include:
            - Artist names
            - Album names
            - Song titles
            - Countries
            - Nationalities
            - Languages
            - Years
            - Decades
            - User opinions (favorite, amazing, underrated, etc.)
            - Listening events (seen live, concert, etc.)
            - Playlist labels
            - Personal labels
            - Meme or joke tags

            ======================================
            EXAMPLES
            ======================================

            hip hop            -> Hip Hop
            rap                -> Hip Hop
            conscious rap      -> Conscious Hip Hop
            trap               -> Trap
            female singer      -> Female Vocalist
            ambient            -> Ambient
            usa                -> Ignore
            favorite           -> Ignore
            seen live          -> Ignore

            ======================================
            RESPONSE FORMAT
            ======================================

            Return ONLY valid JSON.

            Do NOT include markdown.
            Do NOT include code fences.
            Do NOT include explanations.
            Do NOT include reasoning.
            Do NOT include any text before or after the JSON.

            Return a JSON array containing exactly one object for every input tag.

            The order of the output objects MUST match the order of the input tags.

            Return exactly this structure:

            [
              {
                "rawTag": "...",
                "canonicalTag": "..."
              }
            ]

            ======================================
            RAW TAGS TO CLASSIFY
            ======================================

            """);

        for (Tag tag : unmappedTags) {
            prompt.append("- ")
                    .append(tag.getName())
                    .append("\n");
        }

        return prompt.toString();
    }

    private void appendOntology(
            StringBuilder prompt,
            List<CanonicalTag> canonicalTags
    ) {

        prompt.append("Genres\n");

        for (CanonicalTag tag : canonicalTags) {
            if (tag.getCategory() == CanonicalTagCategory.GENRE
                    && tag.getParent() == null) {

                appendGenre(prompt, canonicalTags, tag, 0);
            }
        }

        prompt.append("\n");

        prompt.append("Musical Traits\n");

        for (CanonicalTag tag : canonicalTags) {
            if (tag.getCategory() == CanonicalTagCategory.MUSICAL_TRAIT) {
                prompt.append("- ")
                        .append(tag.getName())
                        .append("\n");
            }
        }

        prompt.append("\n");

        prompt.append("Vocal Characteristics\n");

        for (CanonicalTag tag : canonicalTags) {
            if (tag.getCategory() == CanonicalTagCategory.VOCAL_CHARACTERISTIC) {
                prompt.append("- ")
                        .append(tag.getName())
                        .append("\n");
            }
        }

        prompt.append("\n");

        prompt.append("Ignore\n");

        for (CanonicalTag tag : canonicalTags) {
            if (tag.getCategory() == CanonicalTagCategory.IGNORE) {
                prompt.append("- ")
                        .append(tag.getName())
                        .append("\n");
            }
        }

        prompt.append("\n");
    }

    private void appendGenre(
            StringBuilder prompt,
            List<CanonicalTag> canonicalTags,
            CanonicalTag current,
            int depth
    ) {

        prompt.append("  ".repeat(depth))
                .append("- ")
                .append(current.getName())
                .append("\n");

        for (CanonicalTag tag : canonicalTags) {
            if (current.equals(tag.getParent())) {
                appendGenre(prompt, canonicalTags, tag, depth + 1);
            }
        }
    }

}
