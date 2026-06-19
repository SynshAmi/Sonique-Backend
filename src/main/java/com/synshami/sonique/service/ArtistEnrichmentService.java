package com.synshami.sonique.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.synshami.sonique.entity.Artist;
import com.synshami.sonique.entity.ArtistTag;
import com.synshami.sonique.entity.Tag;
import com.synshami.sonique.repository.ArtistRepository;
import com.synshami.sonique.repository.ArtistTagRepository;
import com.synshami.sonique.repository.TagRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistEnrichmentService {

    private final SingleArtistEnrichmentService singleArtistEnrichmentService;
    private final ArtistRepository artistRepository;
    private static final Logger logger=LoggerFactory.getLogger(ArtistEnrichmentService.class);

    public void enrichPendingArtists()
    {
        List<Artist> list = artistRepository.findTop100ByLastUpdatedIsNullOrderByIdAsc();

        for(Artist artist : list)
        {
            try {
                singleArtistEnrichmentService.enrichArtist(artist);
            }
            catch (Exception e) {
                logger.error("Failed to enrich artist {}", artist.getName(), e);
            }
        }
    }

}