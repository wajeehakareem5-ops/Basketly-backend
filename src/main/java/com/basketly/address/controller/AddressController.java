package com.basketly.address.controller;

import com.basketly.address.domain.Address;
import com.basketly.address.dto.AddressDto;
import com.basketly.address.repository.AddressRepository;
import com.basketly.common.exception.ResourceNotFoundException;
import com.basketly.common.response.ApiResponse;
import com.basketly.security.CustomUserDetails;
import com.basketly.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressRepository addressRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressDto>>> getAddresses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AddressDto> addresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(userDetails.getUser()).stream()
                .map(AddressDto::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(addresses));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<AddressDto>> createAddress(
            @Valid @RequestBody AddressDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();

        if (dto.isDefault()) {
            addressRepository.findByUserAndIsDefaultTrue(user).ifPresent(oldDef -> {
                oldDef.setDefault(false);
                addressRepository.save(oldDef);
            });
        }

        Address address = Address.builder()
                .user(user)
                .label(dto.getLabel().trim())
                .recipientName(dto.getRecipientName().trim())
                .phone(dto.getPhone().trim())
                .street(dto.getStreet().trim())
                .city(dto.getCity().trim())
                .state(dto.getState().trim())
                .postalCode(dto.getPostalCode().trim())
                .isDefault(dto.isDefault())
                .build();

        address = addressRepository.save(address);
        return ResponseEntity.ok(ApiResponse.ok("Address added successfully", AddressDto.fromEntity(address)));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<AddressDto>> updateAddress(
            @PathVariable UUID id,
            @Valid @RequestBody AddressDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        Address address = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));

        if (dto.isDefault() && !address.isDefault()) {
            addressRepository.findByUserAndIsDefaultTrue(user).ifPresent(oldDef -> {
                oldDef.setDefault(false);
                addressRepository.save(oldDef);
            });
        }

        address.setLabel(dto.getLabel().trim());
        address.setRecipientName(dto.getRecipientName().trim());
        address.setPhone(dto.getPhone().trim());
        address.setStreet(dto.getStreet().trim());
        address.setCity(dto.getCity().trim());
        address.setState(dto.getState().trim());
        address.setPostalCode(dto.getPostalCode().trim());
        address.setDefault(dto.isDefault());

        address = addressRepository.save(address);
        return ResponseEntity.ok(ApiResponse.ok("Address updated successfully", AddressDto.fromEntity(address)));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Address address = addressRepository.findByIdAndUser(id, userDetails.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));

        addressRepository.delete(address);
        return ResponseEntity.ok(ApiResponse.ok("Address deleted successfully", null));
    }
}
