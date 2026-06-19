package com.synshami.sonique.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "artist_tags",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"artist_id", "tag_id"})
    }
)
public class ArtistTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Column(nullable = false)
    private Integer weight;
}