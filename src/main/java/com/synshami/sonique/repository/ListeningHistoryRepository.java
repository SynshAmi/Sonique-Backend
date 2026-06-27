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
import java.util.List;

public interface ListeningHistoryRepository extends JpaRepository<ListeningHistory, Long> {
    boolean existsByUserAndPlayedAt(User user, LocalDateTime playedAt);

    Page<ListeningHistory> findByUserIdOrderByPlayedAtDesc(Long userId, Pageable pageable);

    @Query("""
        SELECT new com.synshami.sonique.dto.TopSong(
            s.name, s.primaryArtist.name, COUNT(h)
        )
        FROM ListeningHistory h JOIN h.song s
        WHERE h.user.id = :userId
        GROUP BY s.id, s.name, s.primaryArtist.id, s.primaryArtist.name
        ORDER BY COUNT(h) DESC""")
    Page<TopSong> findTopSongs(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT new com.synshami.sonique.dto.TopArtist(
            s.primaryArtist.name, COUNT(h)
        )
        FROM ListeningHistory h JOIN h.song s
        WHERE h.user.id = :userId
        GROUP BY s.primaryArtist.id, s.primaryArtist.name
        ORDER BY COUNT(h) DESC""")
    Page<TopArtist> findTopArtists(@Param("userId") Long userId, Pageable pageable);

    long countByUserId(Long userId);

    @Query("""
        SELECT COUNT(DISTINCT h.song.id)
        FROM ListeningHistory h
        WHERE h.user.id = :userId
        """)
    long countUniqueSongs(@Param("userId") Long userId);

    @Query("""
        SELECT COUNT(DISTINCT s.primaryArtist.id)
        FROM ListeningHistory h JOIN h.song s
        WHERE h.user.id = :userId
        """)
    long countUniqueArtists(@Param("userId") Long userId);

    @Query("""
        SELECT h
        FROM ListeningHistory h
        JOIN FETCH h.song s
        JOIN FETCH s.primaryArtist
        WHERE h.user.id = :userId
        ORDER BY h.playedAt DESC
        """)
    List<ListeningHistory> findRecentHistoryWithSongs(
            @Param("userId") Long userId,
            Pageable pageable
    );

    /*@Query("""
    SELECT h
    FROM ListeningHistory h
    JOIN FETCH h.song s
    JOIN FETCH s.primaryArtist
    LEFT JOIN FETCH s.primaryArtist.artistTags at
    LEFT JOIN FETCH at.tag
    WHERE h.user.id = :userId
    ORDER BY h.playedAt DESC
    """)
    List<ListeningHistory> findRecentHistoryWithArtistTags(
            @Param("userId") Long userId,
            Pageable pageable
    );*/

    @Query("""
    SELECT h.id
    FROM ListeningHistory h
    WHERE h.user.id = :userId
    ORDER BY h.playedAt DESC
    """)
    List<Long> findRecentHistoryIdsByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("""
    SELECT DISTINCT h
    FROM ListeningHistory h
    JOIN FETCH h.song s
    JOIN FETCH s.primaryArtist a
    LEFT JOIN FETCH a.artistTags at
    LEFT JOIN FETCH at.tag
    WHERE h.id IN :ids
    ORDER BY h.playedAt DESC
    """)
    List<ListeningHistory> findHistoryWithArtistTagsByIds(
            @Param("ids") List<Long> ids
    );

    @Query("""
    SELECT COALESCE(SUM(s.durationMs), 0)
    FROM ListeningHistory lh
    JOIN lh.song s
    WHERE lh.user.id = :userId
    """)
    Long getTotalListeningDuration(@Param("userId") Long userId);
}
