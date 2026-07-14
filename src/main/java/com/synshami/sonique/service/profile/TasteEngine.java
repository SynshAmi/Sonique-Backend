package com.synshami.sonique.service.profile;

import com.synshami.sonique.dto.profile.TasteEngineResponse;
import com.synshami.sonique.entity.Artist;
import com.synshami.sonique.entity.ListeningHistory;
import com.synshami.sonique.entity.Song;
import com.synshami.sonique.entity.UserTagPreference;
import com.synshami.sonique.enums.CanonicalTagCategory;
import com.synshami.sonique.repository.ListeningHistoryRepository;
import com.synshami.sonique.repository.UserTagPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TasteEngine {
    private final ListeningHistoryRepository listeningHistoryRepository;
    private final ListeningAnalysisService listeningAnalysisService;
    private final UserTagPreferenceRepository userTagPreferenceRepository;

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

        List<UserTagPreference> preferences = userTagPreferenceRepository.findByUserId(userId);

        List<UserTagPreference> genres = new ArrayList<>();
        List<UserTagPreference> traits = new ArrayList<>();
        List<UserTagPreference> vocals = new ArrayList<>();

        for(UserTagPreference preference : preferences) {
            switch (preference.getTag().getCategory()) {
                case GENRE:
                    genres.add(preference);
                    break;
                case MUSICAL_TRAIT:
                    traits.add(preference);
                    break;
                case VOCAL_CHARACTERISTIC:
                    vocals.add(preference);
                    break;

                 default:
                    break;
            }
        }

        genres.sort((UserTagPreference a, UserTagPreference b) -> Double.compare(b.getWeight(), a.getWeight()));
        traits.sort((UserTagPreference a, UserTagPreference b) -> Double.compare(b.getWeight(), a.getWeight()));
        vocals.sort((UserTagPreference a, UserTagPreference b) -> Double.compare(b.getWeight(), a.getWeight()));

        String topGenres=buildTopTagsString(genres, 5);
        String topTraits=buildTopTagsString(traits, 3);
        String topVocals=buildTopTagsString(vocals, 1);

        return new TasteEngineResponse(topArtist!=null?topArtist.getName():null,
                maxArtistPlays, topSong!=null?topSong.getName():null,
                maxSongPlays, topGenres, topTraits, topVocals);
    }

    private String buildTopTagsString(List<UserTagPreference> preferences, int limit) {

        if (preferences.isEmpty()) {
            return null;
        }

        StringBuilder ans = new StringBuilder();

        int count = Math.min(preferences.size(), limit);

        for (int i = 0; i < count; i++) {
            ans.append(preferences.get(i).getTag().getName());

            if (i < count - 1) {
                ans.append(", ");
            }
        }

        return ans.toString();
    }
}
