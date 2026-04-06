package com.synshami.sonique.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.synshami.sonique.config.SpotifyProperties;
import com.synshami.sonique.dto.SpotifyTokenResponse;
import com.synshami.sonique.dto.SpotifyUserProfileResponse;
import com.synshami.sonique.entity.Song;
import com.synshami.sonique.entity.SpotifyToken;
import com.synshami.sonique.entity.User;
import com.synshami.sonique.exception.DuplicateResourceException;
import com.synshami.sonique.repository.SpotifyTokenRepository;
import com.synshami.sonique.repository.UserRepository;
import com.synshami.sonique.service.OAuthStateService;
import com.synshami.sonique.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/spotify")
@RequiredArgsConstructor
public class SpotifyAuthController {

    private final SpotifyProperties spotifyProperties;
    private final OAuthStateService oAuthStateService;
    private final UserRepository userRepository;
    private final SpotifyService spotifyService;
    private final SpotifyTokenRepository spotifyTokenRepository;

    @GetMapping("/connect")
    public ConnectResponse connectSpotify() {

        Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        String state = oAuthStateService.generateState(userId);

        String authUrl = "https://accounts.spotify.com/authorize" +
                "?response_type=code" +
                "&client_id=" + spotifyProperties.getClientId() +
                "&scope=" + URLEncoder.encode(
                spotifyProperties.getScope(),
                StandardCharsets.UTF_8
        ) +
                "&redirect_uri=" + URLEncoder.encode(
                spotifyProperties.getRedirectUri(),
                StandardCharsets.UTF_8
        ) +
                "&state=" + URLEncoder.encode(
                state,
                StandardCharsets.UTF_8
        ) +
                "&show_dialog=true"
        ;

        return new ConnectResponse(authUrl);
    }

    @GetMapping("/callback")
    public String callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state
    ) {

        Long userId = oAuthStateService.validateAndExtractUserId(state);

        spotifyService.handleSpotifyCallback(userId, code);

        return "Spotify Connected Successfully";
    }

    @GetMapping("/test/recent")
    public String testRecentlyPlayed() {

        Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SpotifyToken token = spotifyTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Spotify not connected"));

        spotifyService.ingestRecentlyPlayed(user, token.getAccessToken());

        return "DB Updated";
    }

    public record ConnectResponse(String authUrl) {}
}