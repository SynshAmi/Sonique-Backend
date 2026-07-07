package com.synshami.sonique.dto.ontology;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ontology {

    private List<OntologyNode> genres;

    private List<String> musicalTraits;

    private List<String> vocalCharacteristics;
}