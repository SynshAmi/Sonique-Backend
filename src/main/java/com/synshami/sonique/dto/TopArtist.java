package com.synshami.sonique.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TopArtist {
    private String artistName;
    private long playCount;
}
