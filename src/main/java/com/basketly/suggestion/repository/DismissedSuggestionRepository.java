package com.basketly.suggestion.repository;

import com.basketly.suggestion.domain.DismissedSuggestion;
import com.basketly.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface DismissedSuggestionRepository extends JpaRepository<DismissedSuggestion, UUID> {
    Optional<DismissedSuggestion> findByUserAndSuggestionKey(User user, String suggestionKey);
    boolean existsByUserAndSuggestionKey(User user, String suggestionKey);
    void deleteByUserAndSuggestionKey(User user, String suggestionKey);
}
