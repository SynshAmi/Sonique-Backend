package com.synshami.sonique.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ListeningStyleResponse {

    private double explorationScore;
    private double artistDiversityScore;
    private String dominantTimeWindow;
    private double averageTrackAge;
}
