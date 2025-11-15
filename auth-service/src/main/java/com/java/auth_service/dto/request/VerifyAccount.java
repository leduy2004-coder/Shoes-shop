package com.java.auth_service.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.java.auth_service.utility.enumUtils.OtpStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerifyAccount {
    String email;
    String otp;
    OtpStatus status;
}
