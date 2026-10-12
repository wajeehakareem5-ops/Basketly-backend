package com.basketly.suggestion.domain;

import com.basketly.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dismissed_suggestions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_dismissed_suggestion", columnNames = {"user_id", "suggestion_key"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DismissedSuggestion {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "suggestion_key", nullable = false, length = 100)
    private String suggestionKey;

    @Column(name = "dismissed_at", nullable = false)
    private Instant dismissedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.dismissedAt == null) {
            this.dismissedAt = Instant.now();
        }
    }
}
