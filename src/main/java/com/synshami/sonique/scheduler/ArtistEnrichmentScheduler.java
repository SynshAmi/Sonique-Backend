package com.synshami.sonique.scheduler;

import com.synshami.sonique.service.ArtistEnrichmentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArtistEnrichmentScheduler {

    private final ArtistEnrichmentService artistEnrichmentService;

    private static final Logger logger =
            LoggerFactory.getLogger(ArtistEnrichmentScheduler.class);

    @Scheduled(fixedDelay = 900000)
    public void enrichArtists() {

        logger.info("[ArtistEnrichmentScheduler] Scheduler triggered");

        try {

            artistEnrichmentService.enrichPendingArtists();

        }
        catch (Exception e) {

            logger.error(
                    "[ArtistEnrichmentScheduler] Failed to enrich artists",
                    e
            );

        }
    }
}