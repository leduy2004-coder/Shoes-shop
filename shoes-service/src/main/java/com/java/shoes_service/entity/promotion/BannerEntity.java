package com.java.shoes_service.entity.promotion;

import com.java.shoes_service.entity.common.BaseEntity;
import com.java.shoes_service.utility.BannerSlot;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "banner")
@Getter
@Setter
public class BannerEntity extends BaseEntity {
    String title;
    String imageUrl;
    String nameImage;
    String link;
    boolean active;
    BannerSlot slot;
}
