package com.synshami.sonique.repository;

import com.synshami.sonique.entity.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);

    @Query("""
        SELECT t FROM
        Tag t LEFT JOIN
        TagMapping tm
        ON tm.rawTag=t
        WHERE tm IS NULL""")
    List<Tag> findUnmappedTags(Pageable pageable);
}
