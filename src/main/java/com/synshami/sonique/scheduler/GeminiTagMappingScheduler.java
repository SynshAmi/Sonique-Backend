package com.synshami.sonique.scheduler;

import com.synshami.sonique.service.gemini.GeminiTagMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeminiTagMappingScheduler {

    private final GeminiTagMappingService geminiTagMappingService;
    private static final Logger logger =
            LoggerFactory.getLogger(GeminiTagMappingScheduler.class);

    @Scheduled(fixedDelay = 900000)
    public void generateMappings() {
        logger.info("[GeminiTagMappingScheduler] Scheduler triggered");

        geminiTagMappingService.generateMappings();
    }
}