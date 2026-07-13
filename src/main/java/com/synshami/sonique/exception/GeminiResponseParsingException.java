package com.synshami.sonique.exception;

public class GeminiResponseParsingException extends RuntimeException {

    public GeminiResponseParsingException(String message) {
        super(message);
    }

    public GeminiResponseParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}