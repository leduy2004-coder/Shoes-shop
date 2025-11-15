package com.java.shoes_service.dto.promotion.review;

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
public class ReviewResponse {
    String id;
    String productId;
    ProfileGetResponse user;
    int rating;
    String comment;
    String created;
}
