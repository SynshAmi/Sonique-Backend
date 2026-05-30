package com.synshami.sonique.service;

import com.synshami.sonique.dto.RecentTrack;
import com.synshami.sonique.entity.ListeningHistory;
import com.synshami.sonique.repository.ListeningHistoryRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
}
