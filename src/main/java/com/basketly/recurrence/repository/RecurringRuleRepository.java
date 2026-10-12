package com.basketly.recurrence.repository;

import com.basketly.recurrence.domain.RecurringRule;
import com.basketly.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecurringRuleRepository extends JpaRepository<RecurringRule, UUID> {
    List<RecurringRule> findByUserAndActiveTrue(User user);
    Optional<RecurringRule> findByIdAndUser(UUID id, User user);
}
