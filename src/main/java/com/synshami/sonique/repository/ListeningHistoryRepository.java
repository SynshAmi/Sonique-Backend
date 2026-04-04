package com.synshami.sonique.repository;
import com.synshami.sonique.entity.ListeningHistory;
import com.synshami.sonique.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface ListeningHistoryRepository extends JpaRepository<ListeningHistory, Long> {
    boolean existsByUserAndPlayedAt(User user, LocalDateTime playedAt);
}
