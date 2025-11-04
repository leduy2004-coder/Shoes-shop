package com.java.auth_service.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.java.auth_service.dto.response.UserResponse;
import com.java.auth_service.utility.enumUtils.TokenType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigInteger;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenRequest {
    BigInteger id;
    String token;
    TokenType tokenType;
    boolean revoked;
    boolean expired;
    UserResponse userEntity;
}
