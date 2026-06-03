package com.synshami.sonique.controller;

import com.synshami.sonique.dto.ProfileMetrics;
import com.synshami.sonique.service.ListeningHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {
    private final ListeningHistoryService listeningHistoryService;

    @GetMapping("/metrics")
    public ProfileMetrics metrics()
    {
        Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return listeningHistoryService.getProfileMetrics(userId);
    }
}
