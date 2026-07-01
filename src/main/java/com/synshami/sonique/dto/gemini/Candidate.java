package com.synshami.sonique.dto.gemini;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Candidate {

    private Content content;

    private String finishReason;

    private Integer index;

}