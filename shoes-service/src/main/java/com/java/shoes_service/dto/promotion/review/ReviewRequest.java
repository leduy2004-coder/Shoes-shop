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
public class ReviewRequest {
    String productId;
    int rating;
    String comment;
}
