package com.synshami.sonique.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class RecentTrack {
    private String songName;
    private String artistName;
    private String albumName;
    private LocalDateTime playedAt;
}
