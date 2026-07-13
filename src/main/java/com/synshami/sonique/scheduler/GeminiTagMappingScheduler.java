package com.synshami.sonique.scheduler;

import com.synshami.sonique.service.gemini.GeminiTagMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeminiTagMappingScheduler {

    private final GeminiTagMappingService geminiTagMappingService;

    @Scheduled(fixedDelay = 900000)
    public void generateMappings() {
        geminiTagMappingService.generateMappings();
    }
}