package com.synshami.sonique.controller;

import com.synshami.sonique.dto.gemini.Content;
import com.synshami.sonique.dto.gemini.GenerateContentRequest;
import com.synshami.sonique.dto.gemini.GenerateContentResponse;
import com.synshami.sonique.dto.gemini.Part;
import com.synshami.sonique.service.gemini.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gemini")
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;

    @PostMapping("/test")
    public GenerateContentResponse test() {

        Part part = new Part("Reply with exactly: Hello Sonique");

        Content content = new Content();
        content.setParts(List.of(part));

        GenerateContentRequest request = new GenerateContentRequest();
        request.setContents(List.of(content));

        return geminiService.generateContent(request);
    }
}