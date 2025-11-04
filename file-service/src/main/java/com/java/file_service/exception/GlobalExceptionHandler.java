package com.java.file_service.exception;

import com.java.file_service.dto.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(Exception ex) {
        System.out.println("Lỗi :"+ex.toString());
        var apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingValidation(MethodArgumentNotValidException ex) {
        // Lấy lỗi đầu tiên (nếu có)
        var firstErrorOpt = ex.getBindingResult().getAllErrors().stream().findFirst();

        // Lấy enumKey từ defaultMessage của FieldError/ObjectError
        String enumKey = firstErrorOpt
                .map(org.springframework.validation.ObjectError::getDefaultMessage)
                .orElse(null);

        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;

        try {
            if (enumKey != null) {
                errorCode = ErrorCode.valueOf(enumKey);
            }

            // Thử unwrap sang ConstraintViolation để lấy attributes validator (nếu có)
            attributes = firstErrorOpt
                    .map(err -> {
                        try {
                            ConstraintViolation<?> cv =
                                    err.unwrap(ConstraintViolation.class);
                            return cv.getConstraintDescriptor().getAttributes();
                        } catch (Exception ignore) {
                            return null; // không unwrap được (không phải từ bean validation)
                        }
                    })
                    .orElse(null);

            if (attributes != null) {
                log.info("validation attributes: {}", attributes);
            }
        } catch (IllegalArgumentException ignore) {
            // enumKey không khớp ErrorCode -> giữ INVALID_KEY
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(
                attributes != null
                        ? mapAttribute(errorCode.getMessage(), attributes)
                        : errorCode.getMessage()
        );

        return ResponseEntity.badRequest().body(apiResponse);
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));

        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}