package com.synshami.sonique.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProfileMetrics {

    private double explorationScore;
    private double artistDiversityScore;
    private String dominantTimeWindow;
    private double averageTrackAge;
}
