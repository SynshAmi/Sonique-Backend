package com.synshami.sonique.repository;

import com.synshami.sonique.entity.Tag;
import com.synshami.sonique.entity.TagMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TagMappingRepository extends JpaRepository<TagMapping, Long> {
    Optional<TagMapping> findByRawTag(Tag rawTag);

    @Query("""
        SELECT tm
        FROM TagMapping tm
        JOIN FETCH tm.rawTag
        JOIN FETCH tm.canonicalTag
    """)
    List<TagMapping> findAllWithTags();
}
