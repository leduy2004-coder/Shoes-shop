package com.java.shoes_service.controller;

import com.java.shoes_service.dto.ApiResponse;
import com.java.shoes_service.dto.address.*;
import com.java.shoes_service.service.shipping.AddressPublicApiService;
import com.java.shoes_service.service.shipping.UserAddressService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/public/address")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddressController {

    AddressPublicApiService provinceService;
    UserAddressService userAddressService;

    @GetMapping("/provinces")
    public ApiResponse<List<ProvinceDto>> getProvinces() {
        List<ProvinceDto> list = provinceService.getAllProvinces();
        return ApiResponse.<List<ProvinceDto>>builder().result(list).build();
    }

    @GetMapping("/provinces/{provinceCode}/districts")
    public ApiResponse<List<DistrictDto>> getDistricts(
            @PathVariable int provinceCode
    ) {
        List<DistrictDto> list = provinceService.getDistrictsByProvince(provinceCode);
        return ApiResponse.<List<DistrictDto>>builder().result(list).build();
    }

    @GetMapping("/districts/{districtCode}/wards")
    public ApiResponse<List<WardDto>> getWards(
            @PathVariable int districtCode
    ) {
        List<WardDto> list = provinceService.getWardsByDistrict(districtCode);
        return ApiResponse.<List<WardDto>>builder().result(list).build();

    }

    @PostMapping("/create")
    public ApiResponse<UserAddressResponse> createAddress(@RequestBody UserAddressRequest request) {
        UserAddressResponse resp = userAddressService.createUserAddress(request);
        return ApiResponse.<UserAddressResponse>builder().result(resp).build();
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<UserAddressResponse>> getUserAddresses(
            @PathVariable String userId
    ) {
        List<UserAddressResponse> addresses = userAddressService.getUserAddresses(userId);
        return ApiResponse.<List<UserAddressResponse>>builder().result(addresses).build();
    }

    @DeleteMapping("/{addressId}")
    public ApiResponse<Boolean> deleteUserAddress(
            @PathVariable String addressId
    ) {
        userAddressService.deleteUserAddress(addressId);
        return ApiResponse.<Boolean>builder().result(true).build();
    }
}
