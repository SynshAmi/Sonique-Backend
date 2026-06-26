package com.synshami.sonique.controller;

import com.synshami.sonique.dto.ListeningStyleResponse;
import com.synshami.sonique.dto.TasteEngineResponse;
import com.synshami.sonique.service.profile.ListeningStyleEngine;
import com.synshami.sonique.service.profile.TasteEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {
    private final ListeningStyleEngine listeningStyleEngine;
    private final TasteEngine tasteEngine;

    @GetMapping("/metrics")
    public ListeningStyleResponse metrics()
    {
        Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return listeningStyleEngine.getListeningStyle(userId);
    }

    @GetMapping("/taste")
    public TasteEngineResponse taste() {
        Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return tasteEngine.getTasteMetrics(userId);
    }
}
