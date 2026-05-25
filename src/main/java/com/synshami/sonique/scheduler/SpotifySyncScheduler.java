package com.synshami.sonique.scheduler;

import com.synshami.sonique.dto.SpotifyTokenResponse;
import com.synshami.sonique.entity.SpotifyToken;
import com.synshami.sonique.entity.User;
import com.synshami.sonique.repository.SpotifyTokenRepository;
import com.synshami.sonique.service.SpotifyService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SpotifySyncScheduler {

    private final SpotifyService spotifyService;
    private final SpotifyTokenRepository spotifyTokenRepository;
    private static final Logger logger = LoggerFactory.getLogger(SpotifySyncScheduler.class);

    @Scheduled(fixedRate = 15000)
    public void syncUsers() {
        logger.info("Scheduler triggered");

        List<SpotifyToken> tokens = spotifyTokenRepository.findAll();

        for(SpotifyToken token : tokens) {
            try{
                User user=token.getUser();

                if(token.getExpiresAt().isBefore(LocalDateTime.now())) {

                    logger.info("Token expired for user {}", user.getId());

                    SpotifyTokenResponse refreshed =
                            spotifyService.refreshAccessToken(token.getRefreshToken());

                    token.setAccessToken(refreshed.getAccessToken());

                    token.setExpiresAt(
                            LocalDateTime.now()
                                    .plusSeconds(refreshed.getExpiresIn())
                    );

                    spotifyTokenRepository.save(token);
                }

                logger.info("Syncing user {}", user.getId());
                spotifyService.ingestRecentlyPlayed(user, token.getAccessToken());
            }
            catch(Exception e){
                logger.error("Failed for userId: {}", token.getUser().getId(), e);
            }
        }
    }
}