package com.synshami.sonique.repository;

import com.synshami.sonique.entity.Tag;
import com.synshami.sonique.entity.TagMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagMappingRepository extends JpaRepository<TagMapping, Long> {
    Optional<TagMapping> findByRawTag(Tag rawTag);
}
