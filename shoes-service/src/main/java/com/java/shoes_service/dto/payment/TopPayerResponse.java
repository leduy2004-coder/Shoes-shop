package com.java.shoes_service.dto.payment;

import com.java.ProfileGetResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TopPayerResponse {
    String userId;
    Long totalAmount; // Tổng số tiền đã thanh toán
    Integer paymentCount; // Số lần thanh toán
    ProfileGetResponse user; // Thông tin user (được set sau)
}

