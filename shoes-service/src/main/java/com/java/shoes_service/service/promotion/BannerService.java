package com.java.shoes_service.service.promotion;

import com.java.ImageType;
import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.promotion.banner.BannerRequest;
import com.java.shoes_service.dto.promotion.banner.BannerResponse;
import com.java.shoes_service.entity.promotion.BannerEntity;
import com.java.shoes_service.exception.AppException;
import com.java.shoes_service.exception.ErrorCode;
import com.java.shoes_service.repository.promotion.BannerRepository;
import com.java.shoes_service.repository.httpClient.FileClient;
import com.java.shoes_service.utility.BannerSlot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BannerService {
    BannerRepository bannerRepository;
    ModelMapper modelMapper;
    FileClient fileClient;

    public PageResponse<BannerResponse> searchBanner(int page, int size, String title) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page - 1),
                Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "createdDate")   // sort mặc định: mới nhất trước
        );

        Page<BannerEntity> p;
        if (title == null || title.isBlank()) {
            p = bannerRepository.findAll(pageable);
        } else {
            String pattern = ".*" + java.util.regex.Pattern.quote(title.trim()) + ".*";
            p = bannerRepository.findByTitleRegexIgnoreCase(pattern, pageable);
        }

        List<BannerResponse> items = p.getContent()
                .stream()
                .map(this::toDto)
                .toList();

        return new PageResponse<>(
                page,
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages(),
                items
        );
    }

    public BannerResponse getBannerBySlot(BannerSlot slot) {
        BannerEntity entity = bannerRepository.findBySlotAndActive(slot, true)
                .orElseThrow(() -> new AppException(ErrorCode.BANNER_NOT_FOUND));

        return toDto(entity);
    }

    public BannerResponse createOrUpdate(BannerRequest request, MultipartFile file) {

        // Tìm banner theo slot (mỗi slot chỉ có 1 banner)
        BannerEntity banner = bannerRepository.findBySlot(request.getSlot())
                .orElseGet(() -> {
                    BannerEntity b = new BannerEntity();
                    b.setSlot(request.getSlot());
                    b.setActive(true);
                    return b;
                });

        // Cập nhật các field từ request (title, link, active,...)
        if (request.getTitle() != null ) {
            banner.setTitle(request.getTitle());
        }
        if (request.getLink() != null) {
            banner.setLink(request.getLink());
        }
        if (request.getActive() != null){
            banner.setActive(request.getActive());
        }

        if (file != null) {
            try {
                if (banner.getNameImage() != null && !banner.getNameImage().isBlank()) {
                    fileClient.deleteByNameImage(banner.getNameImage(), ImageType.BANNER);
                }
            } catch (Exception e) {
                // log.warn("Delete old banner image failed for slot {}", banner.getSlot(), e);
            }

            // Upload ảnh mới
            var upload = fileClient.uploadMediaBanner(file, banner.getId() != null ? banner.getId() : banner.getSlot().name())
                    .getResult();
            banner.setImageUrl(upload.getUrl());
            banner.setNameImage(upload.getFileName());
        }

        banner = bannerRepository.save(banner);
        return toDto(banner);
    }
    private BannerResponse toDto(BannerEntity e) {
        return modelMapper.map(e, BannerResponse.class);
    }

}

