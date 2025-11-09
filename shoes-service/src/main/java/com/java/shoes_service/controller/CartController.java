package com.java.shoes_service.controller;

import com.java.shoes_service.dto.ApiResponse;
import com.java.shoes_service.dto.cart.CartGetResponse;
import com.java.shoes_service.dto.cart.CartCreateRequest;
import com.java.shoes_service.exception.AppException;
import com.java.shoes_service.exception.ErrorCode;
import com.java.shoes_service.service.CartService;
import com.java.shoes_service.utility.GetInfo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/cart")
public class CartController {
    CartService cartService;

    @GetMapping
    public ApiResponse<CartGetResponse> getCart() {
        String userId = GetInfo.getLoggedInUserName();
        if (userId == null) {
            throw new AppException(ErrorCode.USER_NOT_VALID);
        }
        CartGetResponse response = cartService.getCart(userId);
        return ApiResponse.<CartGetResponse>builder()
                .result(response)
                .build();
    }

    @PostMapping
    public ApiResponse<CartGetResponse> addToCart(@RequestBody CartCreateRequest req) {
        String userId = GetInfo.getLoggedInUserName();
        if (userId == null) {
            throw new AppException(ErrorCode.USER_NOT_VALID);
        }
        return ApiResponse.<CartGetResponse>builder()
                .result(cartService.addToCart(userId, req))
                .build();
    }

    @PutMapping("/items/{itemId}")
    public ApiResponse<CartGetResponse> updateItemQuantity(
            @PathVariable String itemId,
            @RequestBody CartCreateRequest quantity
    ) {
        String userId = GetInfo.getLoggedInUserName();
        if (userId == null) {
            throw new AppException(ErrorCode.USER_NOT_VALID);
        }
        return ApiResponse.<CartGetResponse>builder()
                .result(cartService.updateItemQuantity(userId, itemId, quantity.getQuantity()))
                .build();
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<CartGetResponse> deleteCartItem(@PathVariable String itemId) {
        String userId = GetInfo.getLoggedInUserName();
        if (userId == null) {
            throw new AppException(ErrorCode.USER_NOT_VALID);
        }
        CartGetResponse response = cartService.deleteCartItem(userId, itemId);
        return ApiResponse.<CartGetResponse>builder()
                .result(response)
                .build();
    }

    @DeleteMapping("/clear")
    public ApiResponse<Void> clearCart() {
        String userId = GetInfo.getLoggedInUserName();
        if (userId == null) {
            throw new AppException(ErrorCode.USER_NOT_VALID);
        }
        cartService.clearCart(userId);
        return ApiResponse.<Void>builder()
                .result(null)
                .build();
    }
}

