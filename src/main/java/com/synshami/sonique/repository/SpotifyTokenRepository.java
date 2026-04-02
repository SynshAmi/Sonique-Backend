package com.synshami.sonique.repository;

import com.synshami.sonique.entity.SpotifyToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpotifyTokenRepository extends JpaRepository<SpotifyToken, Long> {

    Optional<SpotifyToken> findBySpotifyUserId(String spotifyUserId);

    Optional<SpotifyToken> findByUserId(Long userId);

    boolean existsBySpotifyUserId(String spotifyUserId);
}