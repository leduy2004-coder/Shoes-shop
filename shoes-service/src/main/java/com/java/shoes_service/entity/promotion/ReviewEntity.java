package com.java.shoes_service.entity.promotion;

import com.java.shoes_service.entity.common.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "reviews")
public class ReviewEntity extends BaseEntity {
    String productId;
    String userId;
    int rating;
    String comment;
}
