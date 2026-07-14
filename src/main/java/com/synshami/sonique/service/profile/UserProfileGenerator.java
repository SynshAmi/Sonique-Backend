package com.synshami.sonique.service.profile;

import com.synshami.sonique.dto.profile.ListeningStyleResponse;
import com.synshami.sonique.dto.profile.TasteEngineResponse;
import com.synshami.sonique.dto.profile.UserStats;
import com.synshami.sonique.entity.User;
import com.synshami.sonique.entity.UserProfile;
import com.synshami.sonique.enums.TimeWindow;
import com.synshami.sonique.exception.ResourceNotFoundException;
import com.synshami.sonique.repository.UserProfileRepository;
import com.synshami.sonique.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileGenerator {

    private final UserRepository userRepository;
    private final ListeningHistoryService listeningHistoryService;
    private final ListeningStyleEngine listeningStyleEngine;
    private final TasteEngine tasteEngine;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public void generateUserProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserStats stats=listeningHistoryService.getUserStats(userId);
        ListeningStyleResponse style=listeningStyleEngine.getListeningStyle(userId);
        TasteEngineResponse taste=tasteEngine.getTasteMetrics(userId);

        UserProfile profile=userProfileRepository.findById(userId).orElse(UserProfile.builder().user(user).build());

        profile.setTotalTracks((int)stats.getTotalPlays());
        profile.setUniqueTracks((int)stats.getUniqueSongs());
        profile.setUniqueArtists((int)stats.getUniqueArtists());
        profile.setTotalListeningHours(stats.getTotalListeningHours());

        profile.setExplorationScore(style.getExplorationScore());
        profile.setArtistDiversityScore(style.getArtistDiversityScore());
        profile.setAverageTrackAge(style.getAverageTrackAge());
        profile.setDominantTimeWindow(TimeWindow.valueOf(style.getDominantTimeWindow()));

        profile.setTopArtistName(taste.getTopArtistName());
        profile.setTopSongName(taste.getTopSongName());
        profile.setTopGenres(taste.getTopGenres());
        profile.setTopMusicalTraits(taste.getTopMusicalTraits());
        profile.setTopVocalCharacteristic(taste.getTopVocalCharacteristic());

        profile.setLastUpdated(java.time.LocalDateTime.now());

        userProfileRepository.save(profile);

    }

}
