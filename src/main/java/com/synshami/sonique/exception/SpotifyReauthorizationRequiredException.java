package com.synshami.sonique.exception;

public class SpotifyReauthorizationRequiredException extends RuntimeException {
    public SpotifyReauthorizationRequiredException(String message) {
        super(message);
    }
}
