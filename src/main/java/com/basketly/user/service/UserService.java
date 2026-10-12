package com.basketly.user.service;

import com.basketly.common.exception.ResourceNotFoundException;
import com.basketly.user.domain.User;
import com.basketly.user.domain.UserSettings;
import com.basketly.user.dto.UserSettingsDto;
import com.basketly.user.repository.UserRepository;
import com.basketly.user.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserSettingsDto getSettings(User user) {
        UserSettings settings = userSettingsRepository.findById(user.getId())
                .orElseGet(() -> {
                    UserSettings created = UserSettings.builder().user(user).build();
                    return userSettingsRepository.save(created);
                });

        return UserSettingsDto.fromEntity(settings);
    }

    @Transactional
    public UserSettingsDto updateSettings(User user, UserSettingsDto dto) {
        UserSettings settings = userSettingsRepository.findById(user.getId())
                .orElseGet(() -> UserSettings.builder().user(user).build());

        if (dto.getWeeklyBudget() != null) {
            settings.setWeeklyBudget(dto.getWeeklyBudget());
        }
        if (dto.getMonthlyBudget() != null) {
            settings.setMonthlyBudget(dto.getMonthlyBudget());
        }
        if (dto.getWeekStartDay() != null) {
            settings.setWeekStartDay(dto.getWeekStartDay());
        }
        if (dto.getTimezone() != null) {
            settings.setTimezone(dto.getTimezone());
        }
        settings.setEmailNotifications(dto.isEmailNotifications());
        settings.setBudgetAlerts(dto.isBudgetAlerts());

        settings = userSettingsRepository.save(settings);
        return UserSettingsDto.fromEntity(settings);
    }

    @Transactional
    public void changePassword(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password does not match");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
