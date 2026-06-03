package com.synshami.sonique.service;

import com.synshami.sonique.dto.ProfileMetrics;
import com.synshami.sonique.entity.ListeningHistory;
import com.synshami.sonique.repository.ListeningHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ListeningHistoryRepository listeningHistoryRepository;

    public ProfileMetrics getProfileMetrics(Long userId)
    {

        List<ListeningHistory> history=listeningHistoryRepository.findRecentHistoryWithSongs(userId, PageRequest.of(0, 500));
        if(history.isEmpty())
        {
            return new ProfileMetrics(0.0, 0.0);
        }

        int totalPlays=history.size();

        Set<Long> uniqueSongs=new HashSet<>();
        Set<String> uniqueArtists=new HashSet<>();

        for(ListeningHistory h : history)
        {
            uniqueSongs.add(h.getSong().getId());
            uniqueArtists.add(h.getSong().getArtistName());
        }

        int uniqueSongsNo=uniqueSongs.size();
        int uniqueArtistsNo=uniqueArtists.size();

        double explorationScore=(double)uniqueSongsNo/ totalPlays;
        double artistDiversityScore=(double)uniqueArtistsNo/ totalPlays;

        explorationScore =
                Math.round(explorationScore * 100.0) / 100.0;
        artistDiversityScore =
                Math.round(artistDiversityScore * 100.0) / 100.0;

        return new ProfileMetrics(explorationScore, artistDiversityScore);
    }
}
