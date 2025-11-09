package com.java.shoes_service.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@Slf4j
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_KEY(1001, "Invalid key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USER_NOT_VALID(1003, "User not valid", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1004, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1005, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1006, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1007, "Unauthenticated", HttpStatus.UNAUTHORIZED),          // 401
    UNAUTHORIZED(1008, "You do not have permission", HttpStatus.FORBIDDEN),     // 403 (giữ lại nếu đã dùng)
    FORBIDDEN(1009, "You do not have permission", HttpStatus.FORBIDDEN),        // 403 (bổ sung để khớp nơi dùng FORBIDDEN)

    INVALID_DOB(1010, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    TOKEN_INVALID(1011, "Invalid token", HttpStatus.BAD_REQUEST),
    RE_TOKEN_EXPIRED(1012, "Expired refresh token", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1013, "Expired token", HttpStatus.BAD_REQUEST),

    UPLOAD_FAILED(1014, "Failed to upload file", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(1015, "File size exceeds the limit of 2MB", HttpStatus.BAD_REQUEST),
    INVALID_FILE_EXTENSION(1016, "Invalid file extension. Allowed extensions are jpg, png, gif, bmp", HttpStatus.BAD_REQUEST),

    ALREADY_FOLLOWING(1017, "Already followed", HttpStatus.BAD_REQUEST),
    NOT_FOLLOWING(1018, "Unfollowed", HttpStatus.BAD_REQUEST),

    PASSWORD_WRONG(1019, "Incorrect current password", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_SAME(1020, "New password and confirmation password do not match", HttpStatus.BAD_REQUEST),

    PRODUCT_USER_NOT_EXISTED(1021, "Product does not exist or does not belong to the user", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_UPDATE(1023, "Product not updated", HttpStatus.BAD_REQUEST),

    // Domain: product / category / brand / banner / review / variant
    PRODUCT_NOT_EXISTED(1851, "Product not existed", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_EXISTED(1852, "Category not existed", HttpStatus.BAD_REQUEST),
    COMMENT_NOT_EXISTED(1853, "Comment not existed", HttpStatus.BAD_REQUEST),
    BRAND_NOT_FOUND(1854, "Brand not existed", HttpStatus.BAD_REQUEST),
    BANNER_NOT_FOUND(1855, "Banner not existed", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1856, "Request not valid", HttpStatus.BAD_REQUEST),
    VARIANT_NOT_FOUND(1857, "Variant not found", HttpStatus.BAD_REQUEST),
    EXCEED_STOCK(1858, "Requested quantity exceeds available stock", HttpStatus.BAD_REQUEST),
    VARIANT_IN_CART(1859, "Cannot delete variant: it is referenced in cart items", HttpStatus.CONFLICT),
    // Cart-related (bổ sung)
    CART_NOT_FOUND(1860, "Cart not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(1861, "Cart item not found", HttpStatus.NOT_FOUND);

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
