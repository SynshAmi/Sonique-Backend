package com.synshami.sonique.repository;

import com.synshami.sonique.entity.Artist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Optional<Artist> findBySpotifyId(String spotifyId);

}