package com.java.shoes_service.exception;



import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@Slf4j
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    PASSWORD_WRONG(1017, "Incorrect current password", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_SAME(1018, "New password and confirmation password do not match", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_EXISTED(1851, "Product not existed", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_EXISTED(1852, "Category not existed", HttpStatus.BAD_REQUEST),
    COMMENT_NOT_EXISTED(1853, "Music not existed", HttpStatus.BAD_REQUEST),
    BRAND_NOT_FOUND(1853, "Brands not existed", HttpStatus.BAD_REQUEST),
    BANNER_NOT_FOUND(1854, "Banner not existed", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1855, "Review not valid", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND), //404
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED), //401
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN), //403
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    TOKEN_INVALID(1009, "Invalid token ", HttpStatus.BAD_REQUEST),
    RE_TOKEN_EXPIRED(1010, "Expired refresh token", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(1011, "Expired refresh token", HttpStatus.BAD_REQUEST),
    UPLOAD_FAILED(1012, "Failed to upload file", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(1013, "File size exceeds the limit of 2MB", HttpStatus.BAD_REQUEST), // 400
    INVALID_FILE_EXTENSION(1014, "Invalid file extension. Allowed extensions are jpg, png, gif, bmp", HttpStatus.BAD_REQUEST), // 400
    ALREADY_FOLLOWING(1015, "Already followed", HttpStatus.BAD_REQUEST),
    NOT_FOLLOWING(1016, "Unfollowed", HttpStatus.BAD_REQUEST),
    PRODUCT_USER_NOT_EXISTED(1021, "Product does not exist or does not belong to the user", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_UPDATE(1023, "Product not updated", HttpStatus.BAD_REQUEST),
    COMMENT_NOT_OWNED(7002, "You are not the owner of this comment", HttpStatus.BAD_REQUEST),

    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}