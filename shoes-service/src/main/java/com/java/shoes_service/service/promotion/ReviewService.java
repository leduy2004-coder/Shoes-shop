package com.java.shoes_service.service.promotion;

import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.promotion.review.ReviewRequest;
import com.java.shoes_service.dto.promotion.review.ReviewResponse;
import com.java.shoes_service.entity.promotion.ReviewEntity;
import com.java.shoes_service.exception.AppException;
import com.java.shoes_service.exception.ErrorCode;
import com.java.shoes_service.repository.httpClient.ProfileClient;
import com.java.shoes_service.repository.product.ProductRepository;
import com.java.shoes_service.repository.promotion.ReviewRepository;
import com.java.shoes_service.service.DateTimeFormatter;
import com.java.shoes_service.utility.GetInfo;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewService {

    ReviewRepository reviewRepository;
    ProductRepository productRepository;
    ModelMapper modelMapper;
    DateTimeFormatter dateTimeFormatter;
    ProfileClient profileClient;

    public PageResponse<ReviewResponse> getReviewByProduct(
            int page, int size, String sort, String productId
    ) {
        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }

        Sort s = resolveSort(sort);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size), s);

        Page<ReviewEntity> p = reviewRepository.findByProductId(productId.trim(), pageable);

        List<ReviewResponse> items = p.getContent().stream()
                .map(this::toDto)
                .toList();

        return new PageResponse<>(
                page,
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages(),
                items
        );
    }

    public ReviewResponse create(ReviewRequest request) {
        if (request.getProductId() == null || request.getProductId().isBlank()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        var product = productRepository.findById(request.getProductId().trim())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        String userId = GetInfo.getLoggedInUserName();
        if (userId == null || userId.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        ReviewEntity entity = ReviewEntity.builder()
                .productId(product.getId())
                .userId(userId)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        entity = reviewRepository.save(entity);

        updateAverageRating(product.getId());

        return toDto(entity);
    }

    public void delete(String reviewId) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));

        String currentUserId = GetInfo.getLoggedInUserName();
        boolean isAdmin = GetInfo.isAdmin();

        if (!(isAdmin || (currentUserId != null && currentUserId.equals(review.getUserId())))) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        reviewRepository.deleteById(reviewId);
        updateAverageRating(review.getProductId());
    }

    // ------------ helpers ------------

    private ReviewResponse toDto(ReviewEntity e) {
        ReviewResponse dto = modelMapper.map(e, ReviewResponse.class);
        dto.setUser(profileClient.getProfile(e.getUserId()).getResult());
        dto.setCreated(dateTimeFormatter.format(e.getCreatedDate()));
        return dto;
    }

    private void updateAverageRating(String productId) {
        long count = reviewRepository.countByProductId(productId);
        int avg;
        if (count > 0) {
            int sum = reviewRepository.findRatingsByProductId(productId).stream()
                    .mapToInt(ReviewEntity::getRating)
                    .sum();
            avg = (int) Math.round((double) sum / count);
        } else {
            avg = 0;
        }
        productRepository.findById(productId).ifPresent(p -> {
            p.setAverageRating(avg);
            productRepository.save(p);
        });
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdDate");
        }
        String[] parts = sort.split(",", 2);
        String field = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (field) {
            case "createdDate", "rating" -> Sort.by(dir, field);
            default -> Sort.by(Sort.Direction.DESC, "createdDate");
        };
    }
}
