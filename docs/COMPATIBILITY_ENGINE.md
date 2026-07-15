# Compatibility Engine V1

> Engineering Design Specification | Sonique

## Overview

The Compatibility Engine is the deterministic comparison layer of Sonique that compares musical identities to assess compatibility between users. Rather than analyzing raw listening history, it operates on normalized, ontology-aware user profiles that represent each user's musical identity through two orthogonal dimensions: listening style and musical taste.

Listening Style describes how users experience music, while Musical Taste describes what they enjoy. Together, these dimensions provide a holistic representation of musical identity that captures both behavioral patterns and preference alignment.

The engine computes compatibility scores on demand, never storing results in persistent storage. All outputs are deterministic: identical inputs always produce identical results. Compatibility assessment is grounded in measurable, explainable signals rather than opaque probabilistic models. When explanation is required, the deterministic results are passed to Gemini for natural language generation, but compatibility computation remains entirely independent of AI inference.

## Design Philosophy

The Compatibility Engine adheres to the following foundational principles:

### Determinism Over Probability
All compatibility assessments are deterministic. Given the same pair of user identities, the engine will always produce identical results. This principle enables reproducibility, debugging, and confidence in the system's behavior for production use.

The decision to use deterministic computation rather than AI-generated predictions addresses fundamental concerns in production systems. Deterministic scoring allows product teams to audit compatibility results, identify bugs, and understand why specific users are matched or not matched. AI-generated predictions, by contrast, obscure reasoning and may produce inconsistent results as underlying models are updated. For a system whose core value proposition is *explainability*, determinism is not optional. It is foundational.

### Explainability Over Opacity
Every compatibility score must be accompanied by clear, structured reasoning. The engine does not return black-box percentages; it returns scores alongside the specific signals that contributed to them. This enables users to understand *why* they are or are not compatible with another user.

### Identity-Based Comparison
Compatibility is computed by comparing deterministic user profiles, not raw listening history. This abstraction layer allows the engine to operate on normalized, semantically-rich representations of musical identity rather than on potentially noisy or incomplete historical data.

### AI-Assisted Explanation, Not AI-Determined Compatibility
Gemini is employed solely to convert deterministic compatibility outputs into human-readable narratives. The AI never influences the compatibility score itself. This separation of concerns ensures that compatibility computation remains transparent, reproducible, and free from black-box learning artifacts.

### Identity-First Architecture
The engine prioritizes the accuracy and richness of user identity representation over the complexity of the comparison logic. A user whose profile is precisely computed will achieve more meaningful compatibility results than a user whose profile is incomplete or imprecise.

## System Context

The Compatibility Engine occupies a specific position within the broader Sonique pipeline:

```
Spotify Listening History
        │
        ▼
Metadata Enrichment
        │
        ▼
Metadata Normalization
        │
        ▼
Canonical Ontology
        │
        ▼
User Identity Profile
        │
        ▼
Compatibility Engine ◄──── On-Demand Computation
        │
        ▼
Compatibility Report
        │
        ▼
Gemini Explanation (Optional)
```

The engine receives two fully-formed user identities as input. It compares them along two dimensions, produces structured compatibility data, and optionally passes those results to Gemini for explanation generation. The engine itself has no dependency on Gemini or any external service; it remains fully functional even if explanation generation becomes unavailable.

---

**Design Decision: On-Demand Computation**

Compatibility scores are computed at request time, never pre-computed and stored in the database. This design choice reflects several production engineering principles:

1. **Freshness**: As user profiles evolve (new listening data, updated preferences), compatibility results automatically reflect the latest identities without invalidation logic.
2. **Storage Efficiency**: Pairwise compatibility matrices would grow quadratically with user count, becoming prohibitively expensive for large user bases.
3. **Simplicity**: On-demand computation eliminates cache invalidation, stale data concerns, and consistency issues between stored results and evolving identities.
4. **Auditability**: Each computation is independent and repeatable, enabling investigators to reproduce specific results for debugging or validation.

The performance cost of on-demand computation is acceptable because the engine is optimized for efficiency and operates on normalized identity data, not raw listening history.

---

## Compatibility Dimensions

Overall compatibility is composed of two equal pillars, each contributing 50% to the final score:

```
        Compatibility
               │
    ┌──────────┴──────────┐
    │                     │
Listening Style      Musical Taste
    │                     │
   50%                    50%
    │                     │
    └──────────┬──────────┘
               │
         Overall Score
```

This two-pillar approach recognizes that musical compatibility operates on two distinct but equally important axes. Two users might share similar listening habits but divergent taste preferences, or vice versa. A complete compatibility assessment must surface both signals.

The overall score should never obscure the individual scores. Both dimensions should be visible in the compatibility output, allowing users to understand the nuanced relationship between their profiles.

## Compatibility Engine Architecture

The engine operates on two user identities and produces compatibility signals across both dimensions:

```
        User A Identity                User B Identity
                │                              │
                └──────────────┬───────────────┘
                               │
                    Compatibility Engine
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
   Listening Style        Musical Taste        Shared Signals
        │                      │                      │
 Exploration            Genre Matching          Convergence
 Diversity              Trait Matching          Divergence
 Track Age              Vocal Matching          Overlap %
 Time Window            Ontology Scoring       Unique Traits
        │                      │                      │
        50%                    50%                   (Signals)
        │                      │                      │
        └──────────────────────┼──────────────────────┘
                               │
                    Overall Compatibility Score
                               │
                    (If Enabled) Gemini Explanation
```

---

## Listening Style Compatibility

**Listening Style measures HOW users experience music: the consumption patterns, exploration behaviors, and temporal contexts of listening.** This is fundamentally distinct from what music they enjoy.

**Musical Taste, conversely, measures WHAT users prefer: the specific genres, traits, and vocal qualities they favor.** This distinction is central to the Compatibility Engine's philosophy. Two users can experience music identically (high listening style compatibility) yet prefer entirely different genres (low taste compatibility). Conversely, two users can share a specific genre interest but consume music in entirely different ways.


### Metrics

Listening style compatibility is computed from four metrics, each contributing equally:

1. **Exploration Score**: Measures the propensity to discover new artists and explore unfamiliar territory. Users with similar exploration habits are more likely to appreciate each other's openness or restraint in musical discovery.

2. **Artist Diversity Score**: Measures the breadth of an artist palette. High diversity suggests varied listening habits; low diversity indicates focused devotion to a narrower set of artists. Compatibility is higher when diversity profiles align.

3. **Average Track Age**: Measures the average age of tracks in the listening history. This captures whether a user favors recent releases, classic material, or a balanced mix. Users with similar track age preferences exhibit compatible listening habits.

4. **Dominant Time Window**: Measures the time of day when listening activity peaks (e.g., morning, afternoon, evening, night). This reflects lifestyle patterns and contexts in which users consume music.

### Computation Approach

**Equal Weighting**: All four metrics carry equal importance in the overall listening style score. No single metric dominates the others.

**Continuous Similarity**: Numerical metrics (exploration, diversity, track age) are compared using continuous similarity functions. As the values between two users diverge, the similarity score decreases smoothly rather than falling off a cliff at arbitrary thresholds. This captures the intuition that a user who explores new artists 40% of the time is more similar to someone who explores 45% than to someone who explores 10%.

**No Thresholds**: The engine does not employ hard thresholds or bucket-based matching. This ensures smooth, differentiable compatibility curves and avoids the brittleness of categorical boundaries.

**Circular Temporal Comparison**: The dominant time window is treated as a circular timeline. Adjacent time windows (e.g., morning and afternoon) receive partial similarity to each other, reflecting the reality that time-of-day listening patterns are continuous. Opposite windows (e.g., morning and night) receive minimal similarity. This circular approach avoids artificially penalizing users whose peak listening times are slightly offset.

---

**Design Decision: Equal Metric Importance**

Each metric is assigned equal weight to avoid over-indexing on any single dimension. While empirical evidence might suggest that one metric (e.g., artist diversity) is more predictive of real-world compatibility than another, weighting based on predictive strength introduces several risks:

1. **Brittleness**: Predictive models capture correlations in training data that may not generalize across user populations or time.
2. **Opacity**: Differential weighting requires justification and introduces subjective prioritization into what should be a deterministic system.
3. **Loss of Symmetry**: Unequal weighting can break the symmetry property of compatibility computation.

Equal weighting preserves simplicity, symmetry, and the deterministic character of the engine. It also ensures that no single behavioral signal dominates the compatibility picture. If future evidence strongly suggests that metric importance should be adjusted, the engine can be recalibrated uniformly across all weights, maintaining its fundamental architecture without breaking existing results.

---

## Musical Taste Compatibility

Musical taste compatibility measures alignment in what music users actually prefer. It is computed by comparing the complete normalized musical identity across the canonical ontology.

### Input: Normalized Musical Preferences

Each user's musical identity includes a preference profile that maps tags (genres, traits, vocal characteristics) to weights representing preference intensity. The engine compares these profiles in full, not just top entries. Every preference contributes to the comparison.

### Identity Completeness

The engine deliberately compares complete musical identities rather than only shared interests. A user should not appear highly compatible with another user simply because they share a small subset of music. Compatibility must reflect both areas of overlap and areas of divergence.

This approach intentionally avoids the deceptive behavior common in playlist-based systems, where users with only limited shared interests can produce artificially high compatibility scores. By comparing full identity profiles, Sonique ensures that compatibility assessments reflect the breadth and depth of both users' musical personalities. Two users who share 5% of their taste but differ on 95% will score lower than two users whose tastes are 60% overlapped, even if the absolute number of shared songs is equal. This preserves the distinction between incidental overlap and genuine musical alignment.

### Ontology Awareness

Tags exist within a hierarchical ontology. "Trap" is a child of "Hip Hop," which is itself a child of a broader category. The engine leverages this structure to compute semantic similarity even when users do not share identical tags.

### Matching Priority

When comparing two user preference profiles, the engine identifies semantic matches using the following priority hierarchy:

1. **Exact Match**: Both users prefer the same tag.
2. **Parent-Child Match**: One user prefers a tag and the other prefers a parent or child tag in the ontology.
3. **Sibling Match**: Both users prefer different tags that share a common parent.
4. **No Match**: Tags are semantically unrelated in the ontology.

### One Match Per Preference

Each preference in a user's profile can contribute to the compatibility score through at most one semantic match. A preference for "Trap" cannot simultaneously contribute to both an exact match with "Trap" and a parent-child match through "Hip Hop." The engine selects the strongest available match and applies it.

This constraint prevents double-counting and ensures that each preference is treated as a unit of signal.

### Shared Contribution Bound

When two users share a preference (exact match, parent-child, or sibling), the contribution to compatibility is limited by the lower of their two preference weights. This represents *shared enthusiasm* rather than individual enthusiasm.

**Example**: User A strongly prefers "Indie Rock" (weight: 0.9) and User B weakly prefers "Indie Rock" (weight: 0.2). The shared contribution is capped at 0.2, reflecting that only User B is weakly enthusiastic. The remaining 0.7 of User A's preference represents unshared enthusiasm and should not inflate the compatibility score. However, it should not create a divergence penalty either. It simply represents domain not covered by both users.

---

**Design Decision: Shared Contribution Bounding**

The shared contribution model recognizes that compatibility is about mutual ground, not aggregated enthusiasm. If User A loves indie rock (0.9) and User B merely tolerates it (0.2), they do have common ground, but User A's intense preference is not fully reciprocated.

Bounding by the minimum weight accomplishes several goals:

1. **Prevents Inflation**: Averaging or using the maximum weight would inflate compatibility by counting unshared enthusiasm as shared ground.
2. **Reflects Asymmetry**: One user's strong preference paired with another's weak interest does not constitute strong mutual alignment.
3. **Preserves Semantics**: The shared contribution accurately represents the degree to which both users are enthusiastic about a musical dimension.

This ensures that the compatibility score reflects realistic overlap rather than optimistic perception. The remaining difference (0.7 in the example) is neither ignored nor penalized; it simply represents personal enthusiasm that is not shared, and therefore does not contribute to compatibility.

---

### Divergence and Unique Preferences

When users have divergent taste (when one prefers a tag that the other dislikes or simply ignores), the compatibility score should reflect that divergence. However, this divergence penalty should only apply to semantic mismatches, not to unshared common ground.

Unique preferences (tags one user has that the other lacks) should not be completely ignored. They provide signal about the breadth or specificity of each user's taste. However, they should be downweighted relative to shared preferences.

---

**Design Decision: No Divergence on Semantic Matches**

If User A prefers "Indie Rock" and User B prefers "Rock," both occupy the same semantic space within the ontology. This is a parent-child match and represents genuine overlap. It should never incur a divergence penalty simply because User A is more specific.

The reasoning:

1. **Semantic Equivalence**: Parent-child matches in the ontology represent the same underlying musical dimension, not divergent preferences.
2. **Prevents False Negatives**: Penalizing specificity mismatch would incorrectly label compatible identities as incompatible.
3. **Encourages Precise Identity**: Users should not be discouraged from having specific, articulate musical preferences just because their matches may be more general.

Divergence penalties are reserved for genuine misalignment: when one user prefers a tag (or its semantic family) and another dislikes it or is indifferent. Semantic matches, by definition, represent alignment in the ontological space and are always positive signals.

---

### Category Weights and Specificity

Not all tags are created equal. The engine recognizes three broad categories:

- **Genres**: The primary drivers of taste compatibility. Genres are most important.
- **Musical Traits**: Secondary signals that describe how music is performed or structured. Less important than genres.
- **Vocal Characteristics**: Tertiary signals describing vocal qualities. Least important.

Within genres, specificity matters. Matching the exact subgenre "Trap" is a stronger signal than matching only the parent category "Hip Hop." The engine accounts for this by considering ontological depth: more specific tags carry higher weight than broad parents.

**Example**: A shared "Trap" preference contributes more to compatibility than a shared "Hip Hop" preference, even though both indicate some alignment in hip-hop taste. This reflects the reality that specific subgenre alignment suggests more refined compatibility.

## Compatibility Output and Explainability

The Compatibility Engine returns a structured result containing:

1. **Overall Compatibility Score**: A single 0–100 percentage representing total compatibility.
2. **Listening Style Score**: Individual 0–100 percentage for listening style compatibility.
3. **Musical Taste Score**: Individual 0–100 percentage for taste compatibility.
4. **Compatibility Signals**: Structured data describing the specific reasons for compatibility or incompatibility, including:
   - Shared genres and their weights
   - Shared musical traits
   - Similar listening habits
   - Differences in exploration patterns
   - Divergence in taste
   - Areas of unique preference

### Role of Gemini

If a natural language explanation is required, the deterministic compatibility signals are passed to Gemini, which converts them into human-readable text. Gemini never modifies the scores; it merely articulates the engine's findings.

If Gemini is unavailable or disabled, the engine continues to function normally, returning structured compatibility data. The explanation simply becomes unavailable; the compatibility computation is unaffected.

This separation ensures that:

- Compatibility assessment remains reproducible and auditable.
- System reliability is not dependent on external AI services.
- The explanation layer can be improved or replaced without altering the compatibility logic.

## Design Principles

The Compatibility Engine is grounded in the following design principles:

- **Deterministic**: Identical inputs always produce identical outputs. No randomness, no external state, no learning artifacts.
- **Symmetric**: Compatibility(A, B) always equals Compatibility(B, A). The order of comparison does not matter.
- **Explainable**: Every score is accompanied by structured reasoning. Compatibility is never opaque.
- **Ontology-Aware**: Semantic relationships within the tag hierarchy inform compatibility assessment.
- **Identity-First**: The quality and precision of user profiles directly impact compatibility quality.
- **Extensible**: The engine's architecture allows for additional metrics or comparison strategies without breaking existing logic.
- **Computed On-Demand**: Compatibility scores are generated when requested, never pre-computed and stored.
- **AI-Assisted Explanation Only**: External AI services enhance explanation generation but never influence the compatibility score itself.

## Future Improvements

The V1 architecture establishes a solid foundation for compatibility assessment. Future enhancements might include:

### Capability Expansions

- **Friend Recommendation Engine**: Using deterministic compatibility profiles to recommend musically-aligned users.
- **Group Compatibility Assessment**: Extending pairwise compatibility to assess compatibility within groups of users.
- **Playlist Generation**: Using compatibility profiles to generate playlists tailored to multi-user listening sessions.

### Ontology Evolution

- **Richer Hierarchies**: Deeper, more specific tag ontologies that capture finer-grained musical distinctions.
- **Cross-Dimensional Relationships**: Ontological relationships beyond parent-child (e.g., "Ambient" and "Dreamy" as non-hierarchical associations).
- **Community-Driven Tags**: User-generated or crowdsourced tags that emerge from community listening patterns.

### Data and Analysis

- **Compatibility History**: Tracking how user compatibility evolves over time as profiles change.
- **Community Insights**: Aggregate compatibility metrics across user populations to identify musical clusters or trends.
- **Comparative Analytics**: Detailed breakdowns comparing specific users or user cohorts.

All future improvements will maintain the V1 principles: determinism, explainability, identity-first design, and separation of compatibility computation from AI-assisted explanation.

## Conclusion

The Compatibility Engine is not a friendship predictor or relationship recommender. It does not attempt to forecast whether two users will enjoy each other's company or maintain a lasting connection. Instead, it provides a deterministic, auditable measurement of musical identity similarity between two users.

This distinction is fundamental to Sonique's philosophy. The engine measures what can be measured: the objective alignment between two musical identities across two orthogonal dimensions. It explains *why* those identities align or diverge using structured, reproducible signals. What users do with that information (whether they connect, collaborate, or remain strangers) lies outside the engine's scope.

By grounding compatibility assessment in measurable, auditable signals rather than opaque predictive models, the engine provides users with transparent insight into their musical alignment. The architecture prioritizes clarity, reproducibility, and determinism without sacrificing sophistication. Every score is accompanied by reasoning. Every computation can be audited and reproduced. Every result reflects actual similarity, not probabilistic guessing.

This commitment to explainability, determinism, and identity-first design ensures that compatibility assessment remains a trustworthy foundation of Sonique's product rather than a black-box feature.

