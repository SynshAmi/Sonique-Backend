package com.synshami.sonique.repository;

import com.synshami.sonique.entity.UserTagPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserTagPreferenceRepository extends JpaRepository<UserTagPreference, Long> {
    @Modifying
    @Query("""
        DELETE FROM UserTagPreference u
        WHERE u.user.id = :userId
""")
    void deleteByUserId(@Param("userId") Long userId);

    List<UserTagPreference> findByUserId(Long userId);
}
