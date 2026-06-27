package com.synshami.sonique.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(
        name = "user_tag_preferences",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "tag_id"})
        }
)
public class UserTagPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Column(nullable = false)
    private Double weight;
}