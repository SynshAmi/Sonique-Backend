package com.synshami.sonique.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synshami.sonique.config.SpotifyProperties;
import com.synshami.sonique.dto.SpotifyTokenResponse;
import com.synshami.sonique.exception.AuthenticationException;
import com.synshami.sonique.exception.ResourceNotFoundException;
import com.synshami.sonique.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.synshami.sonique.dto.SpotifyUserProfileResponse;
import com.synshami.sonique.entity.*;
import com.synshami.sonique.exception.DuplicateResourceException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class SpotifyService {

    private final RestTemplate restTemplate;
    private final SpotifyProperties spotifyProperties;
    private final UserRepository userRepository;
    private final SpotifyTokenRepository spotifyTokenRepository;
    private final SongRepository songRepository;
    private final ArtistRepository artistRepository;
    private final ListeningHistoryRepository listeningHistoryRepository;
    private static final Logger logger = LoggerFactory.getLogger(SpotifyService.class);

    public SpotifyTokenResponse exchangeCodeForTokens(String code) {

        String url = "https://accounts.spotify.com/api/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("redirect_uri", spotifyProperties.getRedirectUri());
        formData.add("client_id", spotifyProperties.getClientId());
        formData.add("client_secret", spotifyProperties.getClientSecret());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<SpotifyTokenResponse> response =
                    restTemplate.postForEntity(
                            url,
                            request,
                            SpotifyTokenResponse.class
                    );

            if (response.getBody() == null) {
                throw new IllegalStateException("Spotify token response was empty");
            }

            return response.getBody();

        } catch (RestClientException ex) {
            throw new AuthenticationException("Spotify token exchange failed");
        }
    }

    public SpotifyTokenResponse refreshAccessToken(String refreshToken) {

        String url = "https://accounts.spotify.com/api/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", refreshToken);
        formData.add("client_id", spotifyProperties.getClientId());
        formData.add("client_secret", spotifyProperties.getClientSecret());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<SpotifyTokenResponse> response =
                    restTemplate.postForEntity(
                            url,
                            request,
                            SpotifyTokenResponse.class
                    );

            if (response.getBody() == null) {
                throw new IllegalStateException("Spotify token response was empty");
            }

            return response.getBody();

        } catch (RestClientException ex) {
            throw new AuthenticationException("Spotify token exchange failed");
        }
    }

    public SpotifyUserProfileResponse getSpotifyUserProfile(String accessToken) {

        String url = "https://api.spotify.com/v1/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<SpotifyUserProfileResponse> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            request,
                            SpotifyUserProfileResponse.class
                    );

            if (response.getBody() == null) {
                throw new IllegalStateException("Spotify profile response was empty");
            }

            return response.getBody();

        } catch (RestClientException ex) {
            throw new AuthenticationException("Spotify profile fetch failed");
        }
    }

    public void handleSpotifyCallback(Long userId, String code) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SpotifyTokenResponse tokenResponse =
                exchangeCodeForTokens(code);

        SpotifyUserProfileResponse profile =
                getSpotifyUserProfile(tokenResponse.getAccessToken());

        String spotifyUserId = profile.getId();

        SpotifyToken existingToken =
                spotifyTokenRepository.findBySpotifyUserId(spotifyUserId)
                        .orElse(null);

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(tokenResponse.getExpiresIn());

        if (existingToken != null) {
            if (!existingToken.getUser().getId().equals(userId)) {
                throw new DuplicateResourceException("Spotify account already linked");
            }

            existingToken.setAccessToken(tokenResponse.getAccessToken());
            existingToken.setRefreshToken(tokenResponse.getRefreshToken());
            existingToken.setExpiresAt(expiresAt);

            spotifyTokenRepository.save(existingToken);

        } else {
            SpotifyToken newToken = SpotifyToken.builder()
                    .user(user)
                    .spotifyUserId(spotifyUserId)
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .expiresAt(expiresAt)
                    .build();

            spotifyTokenRepository.save(newToken);
        }
    }

    public JsonNode getRecentlyPlayedTracks(String accessToken) {

        String url = "https://api.spotify.com/v1/me/player/recently-played?limit=50";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            request,
                            String.class
                    );

            if (response.getBody() == null) {
                throw new IllegalStateException("Recently played response was empty");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(response.getBody());

        } catch (Exception ex) {
            throw new RuntimeException("Failed to fetch recently played tracks");
        }
    }

    public Song getOrCreateSong(String spotifyId,
                                String name,
                                Artist artist,
                                String albumName,
                                LocalDate releaseDate,
                                Integer popularity
        ) {

        return songRepository.findBySpotifyId(spotifyId)
                .orElseGet(() -> {
                    Song newSong = Song.builder()
                            .spotifyId(spotifyId)
                            .name(name)
                            .primaryArtist(artist)
                            .albumName(albumName)
                            .releaseDate(releaseDate)
                            .popularity(popularity)
                            .build();

                    return songRepository.save(newSong);
                });
    }

    public void saveListeningHistory(User user,
                                     Song song,
                                     LocalDateTime playedAt) {

        boolean exists = listeningHistoryRepository
                .existsByUserAndPlayedAt(user, playedAt);

        if (exists) {
            return;
        }

        ListeningHistory history = ListeningHistory.builder()
                .user(user)
                .song(song)
                .playedAt(playedAt)
                .build();

        listeningHistoryRepository.save(history);
    }

    public void ingestRecentlyPlayed(User user, String accessToken) {
        logger.info("Ingesting for userId: {}", user.getId());
        JsonNode node = getRecentlyPlayedTracks(accessToken);
        JsonNode items = node.get("items");

        for (JsonNode item : items) {

            JsonNode track = item.get("track");

            String trackId = track.get("id").asText();
            String trackName = track.get("name").asText();
            String artistName = track.get("artists").get(0).get("name").asText();
            String artistId = track.get("artists").get(0).get("id").asText();
            String albumName = track.get("album").get("name").asText();
            Integer popularity=null;

            LocalDate releaseDate = LocalDate.parse(track.get("album").get("release_date").asText());

            String playedAtStr = item.get("played_at").asText();

            LocalDateTime playedAt = Instant.parse(playedAtStr)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .withNano(0);

            Artist artist = getOrCreateArtist(artistId, artistName);

            Song song = getOrCreateSong(
                    trackId,
                    trackName,
                    artist,
                    albumName,
                    releaseDate,
                    popularity
            );

            saveListeningHistory(user, song, playedAt);
        }
    }

    public Artist getOrCreateArtist(String spotifyId, String name){
        return artistRepository.findBySpotifyId(spotifyId).orElseGet(() -> {
            Artist artist = Artist.builder()
                    .spotifyId(spotifyId)
                    .name(name)
                    .rawGenres(new HashSet<>())
                    .popularity(null)
                    .lastUpdated(null)
                    .build();

            return artistRepository.save(artist);
        });
    }

    /*public JsonNode getArtistDetails(String artistId, String accessToken)
    {
        String url="https://api.spotify.com/v1/artists/"+artistId;

        HttpHeaders headers=new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request=new HttpEntity<>(headers);

        try{
            ResponseEntity<String> response=restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            if(response.getBody()==null)
            {
                throw new IllegalStateException("Artist details response was empty");
            }

            ObjectMapper objectMapper=new ObjectMapper();
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch artist details");
        }

    }

    public void enrichArtist(Artist artist)
    {
        logger.info("[ArtistEnrichment] Enriching artist {}", artist.getName());

        SpotifyToken token=spotifyTokenRepository.findTopByOrderByIdAsc().orElse(null);
        if(token == null) return;

        String accessToken=token.getAccessToken();

        JsonNode node=getArtistDetails(artist.getSpotifyId(), accessToken);

        Integer popularity=node.get("popularity").asInt();

        JsonNode genresNode=node.get("genres");

        Set<String> genres=new HashSet<>();
        for(JsonNode genreNode : genresNode)
        {
            genres.add(genreNode.asText());
        }

        artist.setPopularity(popularity);
        artist.setRawGenres(genres);
        artist.setLastUpdated(LocalDateTime.now());

        artistRepository.save(artist);
    }*/
}