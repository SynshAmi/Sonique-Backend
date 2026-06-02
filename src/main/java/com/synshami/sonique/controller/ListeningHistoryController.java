package com.synshami.sonique.controller;

import com.synshami.sonique.dto.RecentTrack;
import com.synshami.sonique.dto.TopArtist;
import com.synshami.sonique.dto.TopSong;
import com.synshami.sonique.service.ListeningHistoryService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class ListeningHistoryController {
    private final ListeningHistoryService listeningHistoryService;

    @GetMapping("/recent")
    public List<RecentTrack> getRecentTracks()
    {
        Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return listeningHistoryService.getRecentTracks(userId);
    }

    @GetMapping("/top-songs")
    public List<TopSong> topSongs()
    {
        Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return listeningHistoryService.getTopSongs(userId);
    }

    @GetMapping("/top-artists")
    public List<TopArtist> topArtists()
    {
        Long userId = (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return listeningHistoryService.getTopArtists(userId);
    }
}
