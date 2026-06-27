package com.synshami.sonique.service;

import com.synshami.sonique.dto.*;
import com.synshami.sonique.entity.ListeningHistory;
import com.synshami.sonique.repository.ListeningHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ListeningHistoryService {
    private final ListeningHistoryRepository listeningHistoryRepository;

    public List<RecentTrack> getRecentTracks(Long userId)
    {
        Page<ListeningHistory> page=listeningHistoryRepository.findByUserIdOrderByPlayedAtDesc(
                userId,
                PageRequest.of(0, 20));

        List<ListeningHistory> histories=page.getContent();
        List<RecentTrack> recentTracks=new ArrayList<>();

        for(ListeningHistory history : histories)
        {
            RecentTrack track=new RecentTrack(history.getSong().getName(),
                    history.getSong().getPrimaryArtist().getName(),
                    history.getSong().getAlbumName(),
                    history.getPlayedAt());

            recentTracks.add(track);
        }

        return recentTracks;
    }

    public List<TopSong> getTopSongs(Long userId)
    {
        Page<TopSong> page=listeningHistoryRepository.findTopSongs(userId, PageRequest.of(0, 10));

        return page.getContent();
    }

    public List<TopArtist> getTopArtists(Long userId)
    {
        Page<TopArtist> page=listeningHistoryRepository.findTopArtists(userId, PageRequest.of(0, 5));

        return page.getContent();
    }

    public UserStats getUserStats(Long userId)
    {
        long totalPlays=listeningHistoryRepository.countByUserId(userId);
        long uniqueSongs= listeningHistoryRepository.countUniqueSongs(userId);
        long uniqueArtists= listeningHistoryRepository.countUniqueArtists(userId);
        long totalListeningDurationMs= listeningHistoryRepository.getTotalListeningDuration(userId);
        double totalListeningHours= Math.round((double) totalListeningDurationMs / (1000 * 60 * 60) * 100.0) / 100.0;

        return new UserStats(totalPlays, uniqueSongs, uniqueArtists, totalListeningHours);
    }
}
