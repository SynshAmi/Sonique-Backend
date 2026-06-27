package com.synshami.sonique.service.profile;

import com.synshami.sonique.entity.Artist;
import com.synshami.sonique.entity.ListeningHistory;
import com.synshami.sonique.entity.Song;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ListeningAnalysisService {
    public Map<Artist, Integer> getArtistFrequencyMap(List<ListeningHistory> history) {
        Map<Artist, Integer> artistCounts = new HashMap<>();
        for (ListeningHistory entry : history) {
            Artist artist = entry.getSong().getPrimaryArtist();
            artistCounts.put(
                    artist,
                    artistCounts.getOrDefault(artist, 0) + 1
            );
        }
        return artistCounts;
    }

    public Map<Song, Integer> getSongFrequencyMap(List<ListeningHistory> history) {
        Map<Song, Integer> songCounts = new HashMap<>();
        for (ListeningHistory entry : history) {
            Song song = entry.getSong();
            songCounts.put(
                    song,
                    songCounts.getOrDefault(song, 0) + 1
            );
        }
        return songCounts;
    }
    
}
