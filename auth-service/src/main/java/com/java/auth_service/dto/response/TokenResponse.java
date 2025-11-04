package com.java.auth_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.java.auth_service.utility.enumUtils.TokenType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {
    private String id;
    private String refreshToken;
    private TokenType tokenType;
    private boolean revoked;
    private boolean expired;
}
