package com.synshami.sonique.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TasteEngineResponse {

    private String topArtistName;
    private int topArtistPlayCount;

    private String topSongName;
    private int topSongPlayCount;
}