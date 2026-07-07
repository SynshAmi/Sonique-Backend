package com.synshami.sonique.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tag_mappings",
        uniqueConstraints = @UniqueConstraint(columnNames = "raw_tag_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "raw_tag_id", unique = true)
    private Tag rawTag;

    @ManyToOne(optional = false)
    @JoinColumn(name = "canonical_tag_id")
    private CanonicalTag canonicalTag;
}