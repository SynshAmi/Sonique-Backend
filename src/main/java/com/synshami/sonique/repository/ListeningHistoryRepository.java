package com.synshami.sonique.repository;

import com.synshami.sonique.entity.ListeningHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListeningHistoryRepository extends JpaRepository<ListeningHistory, Long> {
}
