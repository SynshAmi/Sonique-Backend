package com.synshami.sonique.service.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdentityGenerationService {
    private final UserTagPreferenceEngine userTagPreferenceEngine;
    private final UserProfileGenerator userProfileGenerator;

    public void generate(Long userId)
    {
        userTagPreferenceEngine.generateUserPreferences(userId);
        userProfileGenerator.generateUserProfile(userId);
    }
}
