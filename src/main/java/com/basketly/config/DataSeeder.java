package com.basketly.config;

import com.basketly.address.domain.Address;
import com.basketly.address.repository.AddressRepository;
import com.basketly.user.domain.Role;
import com.basketly.user.domain.User;
import com.basketly.user.domain.UserSettings;
import com.basketly.user.repository.UserRepository;
import com.basketly.user.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${basketly.seeding.enabled:true}")
    private boolean seedingEnabled;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedingEnabled) {
            return;
        }

        seedUser("demo@basketly.app", "Aarav Sharma", Role.USER, new BigDecimal("4500.00"), new BigDecimal("18000.00"));
        seedUser("test@basketly.app", "Diya Patel", Role.USER, new BigDecimal("3500.00"), new BigDecimal("14000.00"));
        seedUser("admin@basketly.app", "Basketly Admin", Role.ADMIN, new BigDecimal("10000.00"), new BigDecimal("40000.00"));
    }

    private void seedUser(String email, String fullName, Role role, BigDecimal weeklyBudget, BigDecimal monthlyBudget) {
        Optional<User> existing = userRepository.findByEmail(email);
        User user;
        if (existing.isPresent()) {
            user = existing.get();
            // Update password hash to guarantee match with Password@123
            user.setPasswordHash(passwordEncoder.encode("Password@123"));
            user.setFullName(fullName);
            user.setRole(role);
            user.setActive(true);
        } else {
            user = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .fullName(fullName)
                    .role(role)
                    .active(true)
                    .build();
        }
        user = userRepository.save(user);

        if (!userSettingsRepository.existsById(user.getId())) {
            UserSettings settings = UserSettings.builder()
                    .user(user)
                    .weeklyBudget(weeklyBudget)
                    .monthlyBudget(monthlyBudget)
                    .build();
            userSettingsRepository.save(settings);
        }

        if (addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user).isEmpty()) {
            Address address = Address.builder()
                    .user(user)
                    .label("Home")
                    .recipientName(fullName)
                    .phone("+91 9876543210")
                    .street("Flat 402, Green Glen Layout, Bellandur")
                    .city("Bengaluru")
                    .state("Karnataka")
                    .postalCode("560103")
                    .isDefault(true)
                    .build();
            addressRepository.save(address);
        }

        log.info("Verified seeded demo user: {}", email);
    }
}
