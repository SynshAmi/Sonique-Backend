package com.synshami.sonique.service.normalization;

import com.synshami.sonique.entity.CanonicalTag;
import com.synshami.sonique.entity.Tag;
import com.synshami.sonique.entity.TagMapping;
import com.synshami.sonique.repository.TagMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagNormalizationService {

    private final TagMappingRepository tagMappingRepository;

    public Optional<CanonicalTag> normalize(Tag rawTag) {
        return tagMappingRepository.findByRawTag(rawTag)
                .map(TagMapping::getCanonicalTag);
    }

}
