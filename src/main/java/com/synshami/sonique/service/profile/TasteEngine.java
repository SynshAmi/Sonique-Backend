package com.synshami.sonique.service.profile;

import com.synshami.sonique.dto.profile.TasteEngineResponse;
import com.synshami.sonique.entity.Artist;
import com.synshami.sonique.entity.ListeningHistory;
import com.synshami.sonique.entity.Song;
import com.synshami.sonique.repository.ListeningHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TasteEngine {
    private final ListeningHistoryRepository listeningHistoryRepository;
    private final ListeningAnalysisService listeningAnalysisService;

    public TasteEngineResponse getTasteMetrics(Long userId) {
        List<ListeningHistory> history=listeningHistoryRepository.findRecentHistoryWithSongs(userId, PageRequest.of(0, 100));

        Map<Artist, Integer> artistCounts = listeningAnalysisService.getArtistFrequencyMap(history);
        Map<Song, Integer> songCounts = listeningAnalysisService.getSongFrequencyMap(history);

        Artist topArtist=null;
        int maxArtistPlays=-1;
        for (Map.Entry<Artist, Integer> entry : artistCounts.entrySet()) {
            if (entry.getValue() > maxArtistPlays) {
                maxArtistPlays = entry.getValue();
                topArtist = entry.getKey();
            }
        }

        Song topSong=null;
        int maxSongPlays=-1;
        for (Map.Entry<Song, Integer> entry : songCounts.entrySet()) {
            if (entry.getValue() > maxSongPlays) {
                maxSongPlays = entry.getValue();
                topSong = entry.getKey();
            }
        }

        return new TasteEngineResponse(topArtist!=null?topArtist.getName():null, maxArtistPlays, topSong!=null?topSong.getName():null, maxSongPlays);
    }
}
