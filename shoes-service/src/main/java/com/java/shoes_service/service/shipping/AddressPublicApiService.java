package com.java.shoes_service.service.shipping;

import com.java.shoes_service.dto.address.DistrictDto;
import com.java.shoes_service.dto.address.ProvinceDto;
import com.java.shoes_service.dto.address.WardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressPublicApiService {

    private static final String BASE_URL = "https://provinces.open-api.vn/api/v1";

    private final RestTemplate restTemplate;

    /**
     * Lấy tất cả tỉnh/thành (không cần districts)
     */
    public List<ProvinceDto> getAllProvinces() {
        String url = BASE_URL + "/p/"; // list provinces
        ResponseEntity<ProvinceDto[]> response =
                restTemplate.getForEntity(url, ProvinceDto[].class);
        ProvinceDto[] body = response.getBody();
        return body != null ? Arrays.asList(body) : List.of();
    }

    /**
     * Lấy danh sách huyện theo mã tỉnh
     */
    public List<DistrictDto> getDistrictsByProvince(int provinceCode) {
        String url = BASE_URL + "/p/" + provinceCode + "?depth=2";
        ProvinceDto province =
                restTemplate.getForObject(url, ProvinceDto.class);
        if (province == null || province.getDistricts() == null) {
            return List.of();
        }
        return province.getDistricts();
    }

    /**
     * Lấy danh sách xã/phường theo mã huyện
     */
    public List<WardDto> getWardsByDistrict(int districtCode) {
        String url = BASE_URL + "/d/" + districtCode + "?depth=2";
        DistrictDto district =
                restTemplate.getForObject(url, DistrictDto.class);
        if (district == null || district.getWards() == null) {
            return List.of();
        }
        return district.getWards();
    }
}
