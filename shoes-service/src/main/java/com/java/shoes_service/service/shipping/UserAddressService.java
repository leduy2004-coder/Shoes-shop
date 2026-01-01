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

        if (request.getProvinceCode() == null
                || request.getDistrictCode() == null
                || request.getWardCode() == null) {
            throw new IllegalArgumentException("Province, district, ward are required");
        }

        if (!StringUtils.hasText(request.getAddressLine())) {
            throw new IllegalArgumentException("addressLine is required");
        }

        // Nếu address mới là default → reset các address cũ
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            List<UserAddress> userAddresses =
                    userAddressRepository.findByUserId(request.getUserId());

            userAddresses.forEach(addr -> addr.setIsDefault(false));
            userAddressRepository.saveAll(userAddresses);
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

    @Transactional
    public UserAddressResponse updateAddressDefault(String addressId, String userId) {
        if (!StringUtils.hasText(addressId)) {
            throw new IllegalArgumentException("addressId is required");
        }
        if (!StringUtils.hasText(userId)) {
            throw new IllegalArgumentException("userId is required");
        }
        
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));

        if (!address.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<UserAddress> userAddresses = userAddressRepository.findByUserId(userId);
        userAddresses.forEach(addr -> addr.setIsDefault(false));
        userAddressRepository.saveAll(userAddresses);

        address.setIsDefault(true);
        UserAddress updated = userAddressRepository.save(address);

        return modelMapper.map(updated, UserAddressResponse.class);
    }
}
