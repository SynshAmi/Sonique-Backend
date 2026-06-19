package com.synshami.sonique.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LastFmServiceTest {
    @Autowired
    private LastFmService lastFmService;

    @Test
    void testArtistInfo()
    {
        JsonNode node = lastFmService.getArtistInfo("Kendrick Lamar");
        System.out.println(node.toPrettyString());
    }

    @Test
    void testArtistTopTags()
    {
        JsonNode node=lastFmService.getArtistTopTags("Kendrick Lamar");
        System.out.println(node.toPrettyString());
    }
}