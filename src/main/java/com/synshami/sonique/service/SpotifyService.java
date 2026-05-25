package com.synshami.sonique.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synshami.sonique.config.SpotifyProperties;
import com.synshami.sonique.dto.SpotifyTokenResponse;
import com.synshami.sonique.exception.AuthenticationException;
import com.synshami.sonique.exception.ResourceNotFoundException;
import com.synshami.sonique.repository.ListeningHistoryRepository;
import com.synshami.sonique.repository.SongRepository;
import com.synshami.sonique.repository.SpotifyTokenRepository;
import com.synshami.sonique.repository.UserRepository;
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
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class SpotifyService {

    private final RestTemplate restTemplate;
    private final SpotifyProperties spotifyProperties;
    private final UserRepository userRepository;
    private final SpotifyTokenRepository spotifyTokenRepository;
    private final SongRepository songRepository;
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
            throw new AuthenticationException("Failed to fetch recently played tracks");
        }
    }

    public Song getOrCreateSong(String spotifyId,
                                String name,
                                String artistName,
                                String albumName) {

        return songRepository.findBySpotifyId(spotifyId)
                .orElseGet(() -> {
                    Song newSong = Song.builder()
                            .spotifyId(spotifyId)
                            .name(name)
                            .artistName(artistName)
                            .albumName(albumName)
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
            String albumName = track.get("album").get("name").asText();
            String playedAtStr = item.get("played_at").asText();

            LocalDateTime playedAt = Instant.parse(playedAtStr)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .withNano(0);

            Song song = getOrCreateSong(
                    trackId,
                    trackName,
                    artistName,
                    albumName
            );

            saveListeningHistory(user, song, playedAt);
        }
    }
}