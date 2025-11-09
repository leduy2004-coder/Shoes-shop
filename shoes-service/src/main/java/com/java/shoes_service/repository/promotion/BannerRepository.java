package com.java.shoes_service.repository.promotion;


import com.java.shoes_service.entity.promotion.BannerEntity;
import com.java.shoes_service.utility.BannerSlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface BannerRepository extends MongoRepository<BannerEntity, String> {
    Optional<BannerEntity> findBySlot(BannerSlot bannerSlot);

    Page<BannerEntity> findByTitleRegexIgnoreCase(String titleRegex, Pageable pageable);
}
