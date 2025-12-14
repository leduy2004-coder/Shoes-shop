package com.java.shoes_service.controller;

import com.java.shoes_service.dto.ApiResponse;
import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.payment.PaymentGetResponse;
import com.java.shoes_service.dto.payment.PaymentRequest;
import com.java.shoes_service.dto.payment.PaymentResponse;
import com.java.shoes_service.dto.payment.TopPayerResponse;
import com.java.shoes_service.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentController {

    @Value("${payment.vnPay.returnUrlAfterPayment}")
    String vnp_ReturnUrl;

    final PaymentService paymentService;

    @GetMapping("/vn-pay")
    public ApiResponse<PaymentResponse> pay(HttpServletRequest request) {
        return ApiResponse.<PaymentResponse>builder().result(paymentService.createVnPayPayment(request)).build();
    }

    @GetMapping("/vn-pay-callback")
    public ApiResponse<PaymentResponse> payCallbackHandler(HttpServletRequest request, HttpServletResponse response,
                                                           @RequestParam(value = "vnp_ResponseCode") String code,
                                                           @RequestParam(value = "vnp_Amount") String amount,
                                                           @RequestParam(value = "vnp_BankCode") String bankCode,
                                                           @RequestParam(value = "variantSizeId") String variantSizeId,
                                                           @RequestParam(value = "userId") String userId
    ) throws IOException {
        response.sendRedirect(vnp_ReturnUrl);
        PaymentResponse paymentDTO;
        // VNPay trả về amount đã nhân 100 (đơn vị xu), cần chia lại cho 100 để lưu số tiền thực tế (VND)
        long amountInVnd = Long.parseLong(amount) / 100L;
        paymentDTO = paymentService.createPaymentProduct(PaymentRequest.builder()
                .variantSizeId(variantSizeId)
                .amount(amountInVnd)
                .code(code)
                .bankCode(bankCode)
                .userId(userId)
                .build());

        return ApiResponse.<PaymentResponse>builder().result(paymentDTO).build();

    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/get-all")
    public ApiResponse<PageResponse<PaymentGetResponse>> getAllPayment(@RequestParam(defaultValue = "1") int page,
                                                                       @RequestParam(defaultValue = "10") int size
    ) {

        PageResponse<PaymentGetResponse> response = paymentService.getPayments(page, size);

        return ApiResponse.<PageResponse<PaymentGetResponse>>builder()
                .result(response)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/top-5-payers")
    public ApiResponse<List<TopPayerResponse>> getTop5Payers() {
        List<TopPayerResponse> response = paymentService.getTop5Payers();
        return ApiResponse.<List<TopPayerResponse>>builder()
                .result(response)
                .build();
    }

    @GetMapping(value = "/detail/{paymentId}")
    public ApiResponse<PaymentGetResponse> getPaymentDetail(@PathVariable String paymentId) {
        PaymentGetResponse response = paymentService.getPaymentDetail(paymentId);
        return ApiResponse.<PaymentGetResponse>builder()
                .result(response)
                .build();
    }
}
