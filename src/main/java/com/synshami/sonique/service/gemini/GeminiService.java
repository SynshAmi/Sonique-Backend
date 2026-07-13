package com.synshami.sonique.service.gemini;

import com.synshami.sonique.config.GeminiProperties;
import com.synshami.sonique.dto.gemini.GenerateContentRequest;
import com.synshami.sonique.dto.gemini.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final RestClient restClient;
    private final GeminiProperties geminiProperties;

    public GenerateContentResponse generateContent(GenerateContentRequest request) {

        String url = geminiProperties.getBaseUrl()
                + "/models/"
                + geminiProperties.getModel()
                + ":generateContent?key="
                + geminiProperties.getApiKey();

        try {

            return restClient
                    .post()
                    .uri(url)
                    .body(request)
                    .retrieve()
                    .body(GenerateContentResponse.class);

        } catch (RestClientException ex) {
            throw new RuntimeException("Failed to communicate with Gemini", ex);
        }
    }
}