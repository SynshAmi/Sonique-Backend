package com.synshami.sonique.scheduler;

import com.synshami.sonique.dto.SpotifyTokenResponse;
import com.synshami.sonique.entity.SpotifyToken;
import com.synshami.sonique.entity.User;
import com.synshami.sonique.enums.SpotifyConnectionStatus;
import com.synshami.sonique.exception.SpotifyReauthorizationRequiredException;
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
        logger.info("[SpotifySyncScheduler] Scheduler triggered");

        List<SpotifyToken> tokens = spotifyTokenRepository.findAllByConnectionStatus(SpotifyConnectionStatus.CONNECTED);

        for(SpotifyToken token : tokens) {
            try{
                User user=token.getUser();

                if(token.getExpiresAt().isBefore(LocalDateTime.now())) {

                    logger.info("[SpotifySyncScheduler] Token expired for user {}", user.getId());

                    SpotifyTokenResponse refreshed =
                            spotifyService.refreshAccessToken(token.getRefreshToken());

                    token.setAccessToken(refreshed.getAccessToken());

                    token.setExpiresAt(
                            LocalDateTime.now()
                                    .plusSeconds(refreshed.getExpiresIn())
                    );

                    spotifyTokenRepository.save(token);
                }

                logger.info("[SpotifySyncScheduler] Syncing user {}", user.getId());
                spotifyService.ingestRecentlyPlayed(user, token.getAccessToken());
            }
            catch(SpotifyReauthorizationRequiredException e)
            {
                logger.info("[SpotifySyncScheduler] Reauthorization required for userId: {}", token.getUser().getId(), e);
                token.setConnectionStatus(SpotifyConnectionStatus.REAUTHORIZATION_REQUIRED);
                spotifyTokenRepository.save(token);
            }
            catch(Exception e){
                logger.error("[SpotifySyncScheduler] Failed for userId: {}", token.getUser().getId(), e);
            }
        }
    }
}