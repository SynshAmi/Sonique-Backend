package com.synshami.sonique.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.synshami.sonique.entity.Artist;
import com.synshami.sonique.entity.ArtistTag;
import com.synshami.sonique.entity.Tag;
import com.synshami.sonique.repository.ArtistRepository;
import com.synshami.sonique.repository.ArtistTagRepository;
import com.synshami.sonique.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SingleArtistEnrichmentService {
    private final LastFmService lastFmService;
    private final ArtistRepository artistRepository;
    private final ArtistTagRepository artistTagRepository;
    private final TagRepository tagRepository;

    @Transactional
    public void enrichArtist(Artist artist) {
        JsonNode info = lastFmService.getArtistInfo(artist.getName());
        JsonNode artistNode = info.path("artist");

        String mbid = artistNode.path("mbid").asText();

        if (mbid.isBlank())
        {
            mbid=null;
        }

        Long listeners=artistNode.path("stats").path("listeners").asLong();

        Long playCount=artistNode.path("stats").path("playcount").asLong();

        artist.setMusicBrainzId(mbid);
        artist.setListenerCount(listeners);
        artist.setPlayCount(playCount);

        JsonNode tagInfo=lastFmService.getArtistTopTags(artist.getName());
        JsonNode tags=tagInfo.path("toptags").path("tag");

        artistTagRepository.deleteByArtist(artist);

        List<ArtistTag> list=new ArrayList<>();

        for(JsonNode tagNode : tags)
        {
            String tagName=tagNode.path("name").asText();
            Integer weight=tagNode.path("count").asInt();

            Tag tag=tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = Tag.builder()
                                .name(tagName)
                                .build();

                        return tagRepository.save(newTag);
                    });

            ArtistTag artistTag= ArtistTag.builder()
                    .artist(artist)
                    .tag(tag)
                    .weight(weight)
                    .build();

            list.add(artistTag);
        }

        artistTagRepository.saveAll(list);

        artist.setLastUpdated(LocalDateTime.now());

        artistRepository.save(artist);

    }
}
