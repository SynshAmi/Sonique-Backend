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
                    history.getSong().getArtistName(),
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

        return new UserStats(totalPlays, uniqueSongs, uniqueArtists);
    }

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
