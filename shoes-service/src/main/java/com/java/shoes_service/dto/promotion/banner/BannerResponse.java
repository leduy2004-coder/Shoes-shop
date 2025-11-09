package com.java.shoes_service.dto.promotion.banner;

import com.java.shoes_service.utility.BannerSlot;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BannerResponse {
    String id;
    String title;
    String imageUrl;
    String nameImage;
    String link;
    boolean active;
    BannerSlot slot;
}
