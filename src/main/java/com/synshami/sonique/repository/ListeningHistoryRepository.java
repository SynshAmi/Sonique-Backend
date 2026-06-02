package com.synshami.sonique.repository;
import com.synshami.sonique.dto.TopArtist;
import com.synshami.sonique.dto.TopSong;
import com.synshami.sonique.entity.ListeningHistory;
import com.synshami.sonique.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ListeningHistoryRepository extends JpaRepository<ListeningHistory, Long> {
    boolean existsByUserAndPlayedAt(User user, LocalDateTime playedAt);

    Page<ListeningHistory> findByUserIdOrderByPlayedAtDesc(Long userId, Pageable pageable);

    @Query("""
        SELECT new com.synshami.sonique.dto.TopSong(
            s.name, s.artistName, COUNT(h)
        )
        FROM ListeningHistory h JOIN h.song s
        WHERE h.user.id = :userId
        GROUP BY s.id, s.name, s.artistName
        ORDER BY COUNT(h) DESC""")
    Page<TopSong> findTopSongs(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT new com.synshami.sonique.dto.TopArtist(
            s.artistName, COUNT(h)
        )
        FROM ListeningHistory h JOIN h.song s
        WHERE h.user.id = :userId
        GROUP BY s.artistName
        ORDER BY COUNT(h) DESC""")
    Page<TopArtist> findTopArtists(@Param("userId") Long userId, Pageable pageable);

}
