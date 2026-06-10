/*package com.synshami.sonique.scheduler;

import com.synshami.sonique.entity.Artist;
import com.synshami.sonique.repository.ArtistRepository;
import com.synshami.sonique.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ArtistEnrichmentScheduler {

    private final SpotifyService spotifyService;
    private final ArtistRepository artistRepository;
    private static final Logger logger = LoggerFactory.getLogger(ArtistEnrichmentScheduler.class);

    @Scheduled(fixedRate = 15000)
    public void enrichArtists()
    {
        logger.info("[ArtistEnrichmentScheduler] Scheduler triggered");
        List<Artist> artists=artistRepository.findTop100ByLastUpdatedIsNullOrderByIdAsc();

        for(Artist artist : artists)
        {
            try
            {
                spotifyService.enrichArtist(artist);
            }
            catch(Exception e)
            {
                logger.error("Failed enrichment for {} with artist id {} and spotify id {}",
                        artist.getName(),
                        artist.getId(),
                        artist.getSpotifyId(),
                        e);
            }
        }
    }
}*/
