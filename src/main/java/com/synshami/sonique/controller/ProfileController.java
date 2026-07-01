package com.synshami.sonique.controller;

import com.synshami.sonique.dto.profile.UserProfileResponse;
import com.synshami.sonique.service.profile.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {
    private final UserProfileService userProfileService;

    @GetMapping
    public UserProfileResponse getProfile() {

        Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userProfileService.getUserProfile(userId);
    }
}
