package com.java.auth_service.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExchangeTokenResponse {

    ExchangeTokenFaceBook faceBookUserInfo;

    ExchangeTokenGoogle googleUserInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ExchangeTokenGoogle {
        String accessToken;
        Long expiresIn;
        String scope;
        String tokenType;
        String idToken;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ExchangeTokenFaceBook {
        String accessToken;
        Long expiresIn;
        String tokenType;
    }
}
