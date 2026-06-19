package com.synshami.sonique.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synshami.sonique.config.LastFmProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class LastFmService {
    private final RestTemplate restTemplate;
    private final LastFmProperties lastFmProperties;

    public JsonNode getArtistInfo(String artistName)
    {
        String url = UriComponentsBuilder
                .fromHttpUrl(lastFmProperties.getBaseUrl())
                .queryParam("method", "artist.getInfo")
                .queryParam("artist", artistName)
                .queryParam("api_key", lastFmProperties.getApiKey())
                .queryParam("format", "json")
                .toUriString();

        try{
            ResponseEntity<String> response=restTemplate.getForEntity(url, String.class);

            if(response.getBody()==null)
            {
                throw new IllegalStateException("Last.fm response was empty");
            }

            ObjectMapper objectMapper=new ObjectMapper();
            return objectMapper.readTree(response.getBody());

        }   catch (RestClientException ex) {
            throw new RuntimeException(
                    "Failed to fetch artist info from Last.fm", ex);
        }
            catch(Exception ex){
            throw new RuntimeException("Failed to parse Last.fm response", ex);
        }
    }

    public JsonNode getArtistTopTags(String artistName)
    {
        String url = UriComponentsBuilder
                .fromHttpUrl(lastFmProperties.getBaseUrl())
                .queryParam("method", "artist.getTopTags")
                .queryParam("artist", artistName)
                .queryParam("api_key", lastFmProperties.getApiKey())
                .queryParam("format", "json")
                .toUriString();

        try{
            ResponseEntity<String> response=restTemplate.getForEntity(url, String.class);

            if(response.getBody()==null)
            {
                throw new IllegalStateException("Last.fm response was empty");
            }

            ObjectMapper objectMapper=new ObjectMapper();
            return objectMapper.readTree(response.getBody());

        }   catch (RestClientException ex) {
            throw new RuntimeException(
                    "Failed to fetch artist tags from Last.fm", ex);
        }
        catch(Exception ex){
            throw new RuntimeException("Failed to parse Last.fm response", ex);
        }
    }
}
