package com.java.shoes_service.dto.banner;

import com.java.shoes_service.utility.BannerSlot;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BannerRequest {
    String title;
    String link;
    BannerSlot slot;
}
