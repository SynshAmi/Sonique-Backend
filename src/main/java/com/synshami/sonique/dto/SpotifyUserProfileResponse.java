package com.synshami.sonique.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyUserProfileResponse {

    private String id;

    @JsonProperty("display_name")
    private String displayName;
}