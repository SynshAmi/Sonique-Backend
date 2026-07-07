package com.synshami.sonique.dto.ontology;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OntologyNode {

    private String name;
    private List<OntologyNode> children;

}
