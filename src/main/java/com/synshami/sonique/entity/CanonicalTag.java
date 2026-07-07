package com.synshami.sonique.entity;

import com.synshami.sonique.enums.CanonicalTagCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "canonical_tags")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CanonicalTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanonicalTagCategory category;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private CanonicalTag parent;
}
