package com.java.shoes_service.service.promotion;

import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.promotion.coupon.CouponGetResponse;
import com.java.shoes_service.dto.promotion.coupon.CouponRequest;
import com.java.shoes_service.dto.promotion.coupon.CouponResponse;
import com.java.shoes_service.entity.promotion.CouponEntity;
import com.java.shoes_service.exception.AppException;
import com.java.shoes_service.exception.ErrorCode;
import com.java.shoes_service.repository.promotion.CouponRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CouponService {
    
    CouponRepository couponRepository;
    ModelMapper modelMapper;

    public CouponResponse create(CouponRequest request) {
        // Kiểm tra mã giảm giá đã tồn tại chưa
        if (couponRepository.findByCode(request.getCode()).isPresent()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Validate expiration date
        if (request.getExpirationDate() != null && request.getExpirationDate().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Validate quantity
        if (request.getQuantity() <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Validate discount percent
        if (request.getDiscountPercent() <= 0 || request.getDiscountPercent() > 100) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        CouponEntity coupon = modelMapper.map(request, CouponEntity.class);
        coupon = couponRepository.save(coupon);
        
        return modelMapper.map(coupon, CouponResponse.class);
    }

    public CouponResponse update(String id, CouponRequest request) {
        CouponEntity coupon = couponRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));

        // Kiểm tra mã code có bị trùng với coupon khác không
        couponRepository.findByCode(request.getCode())
                .ifPresent(existingCoupon -> {
                    if (!existingCoupon.getId().equals(id)) {
                        throw new AppException(ErrorCode.INVALID_REQUEST);
                    }
                });

        // Validate expiration date
        if (request.getExpirationDate() != null && request.getExpirationDate().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Validate quantity
        if (request.getQuantity() <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Validate discount percent
        if (request.getDiscountPercent() <= 0 || request.getDiscountPercent() > 100) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        coupon.setCode(request.getCode());
        coupon.setDiscountPercent(request.getDiscountPercent());
        coupon.setMinOrder(request.getMinOrder());
        coupon.setQuantity(request.getQuantity());
        coupon.setExpirationDate(request.getExpirationDate());
        coupon.setActive(request.isActive());

        coupon = couponRepository.save(coupon);
        return modelMapper.map(coupon, CouponResponse.class);
    }

    public Boolean delete(String id) {
        if (!couponRepository.existsById(id)) {
            return false;
        }

        couponRepository.deleteById(id);
        return true;
    }

    public CouponGetResponse getById(String id) {
        CouponEntity coupon = couponRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
        
        return modelMapper.map(coupon, CouponGetResponse.class);
    }

    public CouponGetResponse getByCode(String code) {
        CouponEntity coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
        
        return modelMapper.map(coupon, CouponGetResponse.class);
    }

    public PageResponse<CouponGetResponse> searchCoupons(
            int page,
            int size,
            String code,
            Boolean active
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page - 1),
                Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "createdDate")
        );

        Page<CouponEntity> p;
        
        if (code != null && !code.isBlank()) {
            String pattern = ".*" + java.util.regex.Pattern.quote(code.trim()) + ".*";
            p = couponRepository.findByCodeRegexIgnoreCase(pattern, pageable);
        } else if (active != null) {
            p = couponRepository.findByActive(active, pageable);
        } else {
            p = couponRepository.findAll(pageable);
        }

        List<CouponGetResponse> items = p.getContent()
                .stream()
                .map(coupon -> modelMapper.map(coupon, CouponGetResponse.class))
                .toList();

        return new PageResponse<>(
                page,
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages(),
                items
        );
    }
}

