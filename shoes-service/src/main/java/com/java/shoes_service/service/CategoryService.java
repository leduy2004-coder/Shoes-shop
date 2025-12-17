package com.java.shoes_service.service;

import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.category.CategoryGetResponse;
import com.java.shoes_service.dto.category.CategoryRequest;
import com.java.shoes_service.dto.category.CategoryResponse;
import com.java.shoes_service.entity.product.CategoryEntity;
import com.java.shoes_service.repository.product.CategoryRepository;
import com.java.shoes_service.repository.product.ProductRepository;
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

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {

    CategoryRepository categoryRepository;
    ProductRepository productRepository;
    ModelMapper modelMapper;

    public PageResponse<CategoryGetResponse> getAll(int page, int size, String name) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size), sort);

        Page<CategoryEntity> p;
        if (name == null || name.isBlank()) {
            p = categoryRepository.findAll(pageable);
        } else {
            // Regex ".*name.*" không phân biệt hoa/thường
            String pattern = ".*" + Pattern.quote(name.trim()) + ".*";
            p = categoryRepository.findByNameRegexIgnoreCase(pattern, pageable);
        }

        List<CategoryGetResponse> items = p.getContent().stream()
                .map(category -> {
                    long count = productRepository.countByCategory_Id(category.getId());
                    return CategoryGetResponse.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .description(category.getDescription())
                            .countProduct(count)
                            .build();
                })
                .toList();

        return new PageResponse<>(page, p.getSize(), p.getTotalElements(), p.getTotalPages(), items);
    }

    public CategoryResponse create(CategoryRequest request) {
        CategoryEntity category = modelMapper.map(request, CategoryEntity.class);
        category = categoryRepository.save(category);
        return modelMapper.map(category, CategoryResponse.class);
    }


    public CategoryResponse update(String id, CategoryRequest request) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        category = categoryRepository.save(category);
        return modelMapper.map(category, CategoryResponse.class);
    }

    public Boolean delete(String id) {

        long count = productRepository.countByCategory_Id(id);
        if (count > 0) {
            return false;
        }

        if (!categoryRepository.existsById(id)) {
            return false;
        }

        categoryRepository.deleteById(id);
        return true;
    }
}
