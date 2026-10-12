package com.basketly.address.repository;

import com.basketly.address.domain.Address;
import com.basketly.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findByUserOrderByIsDefaultDescCreatedAtDesc(User user);
    Optional<Address> findByIdAndUser(UUID id, User user);
    Optional<Address> findByUserAndIsDefaultTrue(User user);
}
