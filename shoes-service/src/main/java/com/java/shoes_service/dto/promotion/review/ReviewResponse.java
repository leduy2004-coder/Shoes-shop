package com.java.shoes_service.dto.promotion.review;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {
    String id;
    String productId;
    String userId;
    int rating;
    String comment;
    String created;
}
