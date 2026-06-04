package com.synshami.sonique.service;

import com.synshami.sonique.dto.ProfileMetrics;
import com.synshami.sonique.entity.ListeningHistory;
import com.synshami.sonique.enums.TimeWindow;
import com.synshami.sonique.repository.ListeningHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
            return new ProfileMetrics(0.0, 0.0, "Not enough data.", 0.0);
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

        int morning=0;
        int afternoon=0;
        int evening=0;
        int night=0;

        for(ListeningHistory h: history)
        {
            int hour=h.getPlayedAt().getHour();
            if(hour>=5 && hour<=11) morning++;
            else if(hour>=12 && hour<=16)    afternoon++;
            else if(hour>=17 && hour<=20)   evening++;
            else    night++;
        }

        TimeWindow dominantTime = TimeWindow.MORNING;
        int max=morning;

        if(afternoon>max){
            dominantTime= TimeWindow.AFTERNOON;
            max=afternoon;
        }
        if(evening>max){
            dominantTime= TimeWindow.EVENING;
            max=evening;
        }
        if(night>max){
            dominantTime= TimeWindow.NIGHT;
            max=night;
        }

        int totalAge=0;
        int currentYear = LocalDate.now().getYear();

        for(ListeningHistory h: history)
        {
            totalAge+= currentYear - h.getSong().getReleaseDate().getYear();
        }

        double averageTrackAge=(double) totalAge/ history.size();

        averageTrackAge=Math.round(averageTrackAge * 100.0)/ 100.0;

        return new ProfileMetrics(explorationScore, artistDiversityScore, String.valueOf(dominantTime), averageTrackAge);
    }
}
