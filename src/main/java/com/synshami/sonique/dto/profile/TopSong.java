package com.synshami.sonique.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TopSong {
    private String songName;
    private String artistName;
    private long playCount;
}
