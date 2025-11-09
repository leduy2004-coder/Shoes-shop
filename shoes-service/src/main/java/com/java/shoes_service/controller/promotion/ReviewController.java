package com.java.shoes_service.controller.promotion;

import com.java.shoes_service.dto.ApiResponse;
import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.promotion.review.ReviewRequest;
import com.java.shoes_service.dto.promotion.review.ReviewResponse;
import com.java.shoes_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/get-by-product")
    public ApiResponse<PageResponse<ReviewResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String product_id,
            @RequestParam(required = false, defaultValue = "createdDate,desc") String sort
    ) {
        var res = reviewService.getReviewByProduct(page, size, sort, product_id);
        return ApiResponse.<PageResponse<ReviewResponse>>builder().result(res).build();
    }

    @PostMapping
    public ApiResponse<ReviewResponse> create(@RequestBody ReviewRequest request) {
        var res = reviewService.create(request);
        return ApiResponse.<ReviewResponse>builder().result(res).build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        reviewService.delete(id);
        return ApiResponse.<Void>builder().build();
    }
}
