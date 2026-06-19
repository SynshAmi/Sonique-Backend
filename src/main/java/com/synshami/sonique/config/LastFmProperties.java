package com.synshami.sonique.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "lastfm")
public class LastFmProperties {

    private String apiKey;

    private String baseUrl;

}
