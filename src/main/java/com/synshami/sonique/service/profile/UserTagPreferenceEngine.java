package com.synshami.sonique.service.profile;

import com.synshami.sonique.entity.*;
import com.synshami.sonique.exception.ResourceNotFoundException;
import com.synshami.sonique.repository.ListeningHistoryRepository;
import com.synshami.sonique.repository.UserRepository;
import com.synshami.sonique.repository.UserTagPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserTagPreferenceEngine {

    private final ListeningHistoryRepository listeningHistoryRepository;
    private final ListeningAnalysisService listeningAnalysisService;
    private final UserTagPreferenceRepository userTagPreferenceRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void generateUserPreferences(Long userId) {

        List<Long> ids = listeningHistoryRepository.findRecentHistoryIdsByUserId(
                userId,
                PageRequest.of(0, 100)
        );

        if (ids.isEmpty()) {
            return;
        }

        List<ListeningHistory> history =
                listeningHistoryRepository.findHistoryWithArtistTagsByIds(ids);

        Map<Artist, Integer> artistCounts = listeningAnalysisService.getArtistFrequencyMap(history);
        Map<Tag, Double> tagWeights = new HashMap<>();

        for (Map.Entry<Artist, Integer> entry : artistCounts.entrySet()) {
            Artist artist = entry.getKey();
            int plays = entry.getValue();

            for (ArtistTag tag : artist.getArtistTags()) {
                double contribution = plays * tag.getWeight();

                tagWeights.put(
                        tag.getTag(),
                        tagWeights.getOrDefault(tag.getTag(), 0.0) + contribution
                );
            }
        }

        double maxWeight = 0;

        for (Double weight : tagWeights.values()) {
            if (weight > maxWeight) {
                maxWeight = weight;
            }
        }

        if (maxWeight == 0) {
            return;
        }

        Map<Tag, Double> normalizedTagWeights = new HashMap<>();

        for (Map.Entry<Tag, Double> entry : tagWeights.entrySet()) {
            Tag tag= entry.getKey();
            Double normalizedWeight = entry.getValue() / maxWeight;

            normalizedTagWeights.put(
                    tag,
                    normalizedWeight
            );
        }

        userTagPreferenceRepository.deleteByUserId(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<UserTagPreference> list = new ArrayList<>();

        for (Map.Entry<Tag, Double> entry : normalizedTagWeights.entrySet()) {
            list.add(
                    UserTagPreference.builder()
                            .user(user)
                            .tag(entry.getKey())
                            .weight(entry.getValue())
                            .build()
            );
        }

        jdbcTemplate.batchUpdate(
                """
                INSERT INTO user_tag_preferences (user_id, tag_id, weight)
                VALUES (?, ?, ?)
                """,
                list,
                list.size(),
                (ps, preference) -> {
                    ps.setLong(1, preference.getUser().getId());
                    ps.setLong(2, preference.getTag().getId());
                    ps.setDouble(3, preference.getWeight());
                }
        );
    }
}
