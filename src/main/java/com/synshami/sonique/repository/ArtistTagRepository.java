package com.synshami.sonique.repository;

import com.synshami.sonique.entity.Artist;
import com.synshami.sonique.entity.ArtistTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistTagRepository extends JpaRepository<ArtistTag, Long> {

    void deleteByArtist(Artist artist);
}
