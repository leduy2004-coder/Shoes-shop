package com.java.shoes_service.service.shipping;

import com.java.shoes_service.dto.address.UserAddressRequest;
import com.java.shoes_service.dto.address.UserAddressResponse;
import com.java.shoes_service.entity.shipping.UserAddress;
import com.java.shoes_service.exception.AppException;
import com.java.shoes_service.exception.ErrorCode;
import com.java.shoes_service.repository.UserAddressRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserAddressService {

    UserAddressRepository userAddressRepository;
    ModelMapper modelMapper;

    @Transactional
    public UserAddressResponse createUserAddress(UserAddressRequest request) {

        if (request.getProvinceCode() == null || request.getDistrictCode() == null || request.getWardCode() == null) {
            throw new IllegalArgumentException("Province, district, ward are required");
        }
        if (request.getAddressLine() == null || request.getAddressLine().isBlank()) {
            throw new IllegalArgumentException("addressLine is required");
        }

        UserAddress userAddress = modelMapper.map(request, UserAddress.class);

        UserAddress saved = userAddressRepository.save(userAddress);

        return modelMapper.map(saved, UserAddressResponse.class);
    }

    public List<UserAddressResponse> getUserAddresses(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("userId is required");
        }
        List<UserAddress> address = userAddressRepository.findByUserId(userId);
        if (address == null) {
            throw new AppException(ErrorCode.ADDRESS_NOT_EXISTED);
        }
        return address.stream()
                .map(userAddress -> modelMapper.map(userAddress, UserAddressResponse.class))
                .collect(Collectors.toList());

    }

    public void deleteUserAddress(String addressId) {
        if (!StringUtils.hasText(addressId)) {
            throw new IllegalArgumentException("addressId is required");
        }

        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));

        userAddressRepository.delete(address);
    }
}
