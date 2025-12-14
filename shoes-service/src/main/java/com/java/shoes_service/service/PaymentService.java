package com.java.shoes_service.service;

import com.java.shoes_service.config.VNPAYConfig;
import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.payment.PaymentGetResponse;
import com.java.shoes_service.dto.payment.PaymentRequest;
import com.java.shoes_service.dto.payment.PaymentResponse;
import com.java.shoes_service.dto.payment.TopPayerResponse;
import com.java.shoes_service.dto.product.product.ProductGetResponse;
import com.java.shoes_service.dto.product.variant.VariantResponse;
import com.java.shoes_service.entity.PaymentEntity;
import com.java.shoes_service.entity.product.ProductEntity;
import com.java.shoes_service.exception.AppException;
import com.java.shoes_service.exception.ErrorCode;
import com.java.shoes_service.repository.PaymentRepository;
import com.java.shoes_service.repository.httpClient.FileClient;
import com.java.shoes_service.repository.httpClient.ProfileClient;
import com.java.shoes_service.repository.product.ProductRepository;
import com.java.shoes_service.service.product.VariantService;
import com.java.shoes_service.utility.GetInfo;
import com.java.shoes_service.utility.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class PaymentService {

    @Value("${payment.vnPay.returnUrl}")
    String vnp_ReturnUrl;

    final PaymentRepository paymentRepository;

    final VNPAYConfig vnPayConfig;
    final ModelMapper modelMapper;
    final ProductRepository productRepository;
    final ProfileClient profileClient;
    final VariantService variantService;
    final FileClient fileClient;


    public PaymentResponse createPaymentProduct(PaymentRequest paymentRequest) {
        PaymentEntity paymentEntity = modelMapper.map(paymentRequest, PaymentEntity.class);
        paymentEntity.setUserId(paymentRequest.getUserId());
        paymentEntity.setVariantSizeId(paymentRequest.getVariantSizeId());
        PaymentEntity savedPaymentEntity = paymentRepository.save(paymentEntity);

        log.info("Payment created successfully for variantSizeId: {}", paymentRequest.getVariantSizeId());
        return modelMapper.map(savedPaymentEntity, PaymentResponse.class);
    }

    public PaymentResponse createVnPayPayment(HttpServletRequest request) {
        try {
            System.out.println("Creating VNPAY Payment...");
            long amount = (long) Integer.parseInt(request.getParameter("amount")) * 100L;
            String bankCode = request.getParameter("bankCode");
            String variantSizeId = request.getParameter("variantSizeId");
            String userId = GetInfo.getLoggedInUserName(); // Lấy userId

            // Kiểm tra giá trị đầu vào
            if (variantSizeId == null || variantSizeId.isEmpty()) {
                throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
            }
            if (userId == null || userId.isEmpty()) {
                throw new AppException(ErrorCode.USER_NOT_EXISTED);
            }

            Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
            vnpParamsMap.put("vnp_Amount", String.valueOf(amount));

            if (bankCode != null && !bankCode.isEmpty()) {
                vnpParamsMap.put("vnp_BankCode", bankCode);
            }

            // Lấy địa chỉ IP
            String ipAddr = VNPayUtil.getIpAddress(request);
            System.out.println("Client IP Address: " + ipAddr);
            vnpParamsMap.put("vnp_IpAddr", ipAddr);

            // Xây dựng URL return
            String returnUrl = this.vnp_ReturnUrl;
            String encodedVariantSizeId = URLEncoder.encode(variantSizeId, StandardCharsets.UTF_8);
            String encodedUserId = URLEncoder.encode(userId, StandardCharsets.UTF_8);

            returnUrl += returnUrl.contains("?") ? "&" : "?";
            returnUrl += "variantSizeId=" + encodedVariantSizeId +
                    "&userId=" + encodedUserId;

            vnpParamsMap.put("vnp_ReturnUrl", returnUrl);
            System.out.println("Return URL: " + returnUrl);

            // Tạo query URL và secure hash
            String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
            String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
            String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
            queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
            String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;

            System.out.println("VNPAY Payment URL created: " + paymentUrl);

            return PaymentResponse.builder()
                    .code("ok")
                    .message("success")
                    .paymentUrl(paymentUrl)
                    .build();
        } catch (Exception e) {
            throw new AppException(ErrorCode.PAYMENT_ERROR);

        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<PaymentGetResponse> getPayments(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdDate").descending());

        var resultPage = paymentRepository.findAll(pageable);
        List<PaymentGetResponse> data = resultPage.getContent()
                .stream()
                .map(this::mapToPaymentGetResponse)
                .toList();

        return new PageResponse<>(page, resultPage.getSize(), resultPage.getTotalElements(),resultPage.getTotalPages(),  data);
    }

    public PaymentGetResponse getPaymentDetail(String paymentId) {
        PaymentEntity paymentEntity = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_EXISTED));
        
        // Kiểm tra quyền truy cập: user chỉ có thể xem payment của chính họ
        String currentUserId = GetInfo.getLoggedInUserName();
        if (currentUserId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        
        // Kiểm tra xem user có phải admin không
        boolean isAdmin = GetInfo.isAdmin();
        if (!isAdmin && !paymentEntity.getUserId().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        return mapToPaymentGetResponse(paymentEntity);
    }

    private PaymentGetResponse mapToPaymentGetResponse(PaymentEntity paymentEntity) {
        PaymentGetResponse response = modelMapper.map(paymentEntity, PaymentGetResponse.class);
        response.setId(paymentEntity.getId());

        // Lấy thông tin user
        try {
            response.setUser(profileClient.getProfile(paymentEntity.getUserId()).getResult());
        } catch (Exception e) {
            log.warn("Could not fetch profile for userId: {}", paymentEntity.getUserId(), e);
            response.setUser(null);
        }

        // Lấy thông tin variant và product từ variantSizeId
        if (paymentEntity.getVariantSizeId() != null && !paymentEntity.getVariantSizeId().isEmpty()) {
            try {
                // Lấy variant từ variantSizeId
                VariantResponse variant = variantService.getVariantById(paymentEntity.getVariantSizeId());
                response.setVariant(variant);

                // Lấy product từ productId trong variant
                if (variant != null && variant.getProductId() != null) {
                    ProductEntity productEntity = productRepository.findById(variant.getProductId()).orElse(null);
                    if (productEntity != null) {
                        ProductGetResponse productResponse = modelMapper.map(productEntity, ProductGetResponse.class);
                        // Lấy ảnh primary
                        try {
                            var imageRes = fileClient.getImage(productEntity.getId(), com.java.ImageType.PRODUCT);
                            if (imageRes != null && imageRes.getResult() != null) {
                                productResponse.setImageUrl(imageRes.getResult().stream()
                                        .filter(com.java.CloudinaryResponse::getIsPrimary)
                                        .findFirst()
                                        .orElse(null));
                            }
                        } catch (Exception e) {
                            log.warn("Could not fetch image for productId: {}", productEntity.getId(), e);
                        }
                        response.setProduct(productResponse);
                    }
                }
            } catch (Exception e) {
                log.warn("Could not fetch variant/product for variantSizeId: {}", paymentEntity.getVariantSizeId(), e);
                response.setVariant(null);
                response.setProduct(null);
            }
        }

        return response;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<TopPayerResponse> getTop5Payers() {
        List<PaymentEntity> allPayments = paymentRepository.findAll();

        // Group by userId và tính tổng amount, đếm số lần thanh toán
        Map<String, List<PaymentEntity>> paymentsByUser = allPayments.stream()
                .filter(payment -> payment.getUserId() != null && payment.getAmount() != null)
                .collect(Collectors.groupingBy(PaymentEntity::getUserId));

        // Tính tổng amount và số lần thanh toán cho mỗi user, sau đó sort và limit 5
        List<TopPayerResponse> topPayers = paymentsByUser.entrySet().stream()
                .map(entry -> {
                    String userId = entry.getKey();
                    List<PaymentEntity> userPayments = entry.getValue();
                    
                    Long totalAmount = userPayments.stream()
                            .mapToLong(p -> p.getAmount() != null ? p.getAmount().longValue() : 0L)
                            .sum();
                    
                    Integer paymentCount = userPayments.size();
                    
                    return TopPayerResponse.builder()
                            .userId(userId)
                            .totalAmount(totalAmount)
                            .paymentCount(paymentCount)
                            .build();
                })
                .sorted(Comparator.comparing(TopPayerResponse::getTotalAmount).reversed())
                .limit(5)
                .collect(Collectors.toList());

        topPayers.forEach(payer -> {
            try {
                payer.setUser(profileClient.getProfile(payer.getUserId()).getResult());
            } catch (Exception e) {
                log.warn("Could not fetch profile for userId: {}", payer.getUserId(), e);
                payer.setUser(null);
            }
        });

        return topPayers;
    }

}
