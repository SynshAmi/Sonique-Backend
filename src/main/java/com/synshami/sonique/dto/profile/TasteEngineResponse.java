package com.synshami.sonique.dto.profile;

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

    private String topGenres;
    private String topMusicalTraits;
    private String topVocalCharacteristic;
}