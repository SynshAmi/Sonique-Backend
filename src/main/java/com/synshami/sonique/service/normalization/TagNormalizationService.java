package com.synshami.sonique.service.normalization;

import com.synshami.sonique.entity.CanonicalTag;
import com.synshami.sonique.entity.Tag;
import com.synshami.sonique.entity.TagMapping;
import com.synshami.sonique.repository.TagMappingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagNormalizationService {

    private final TagMappingRepository tagMappingRepository;
    private final Map<Long, CanonicalTag> cache = new HashMap<>();

    @PostConstruct
    public void reloadCache() {

        cache.clear();
        List<TagMapping> mappings = tagMappingRepository.findAllWithTags();

        for (TagMapping mapping : mappings) {
            cache.put(
                    mapping.getRawTag().getId(),
                    mapping.getCanonicalTag()
            );
        }

    }

    public Optional<CanonicalTag> normalize(Tag rawTag) {
        return Optional.ofNullable(cache.get(rawTag.getId()));
    }

}
