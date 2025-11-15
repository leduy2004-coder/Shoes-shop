package com.java.shoes_service.service;

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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {

    CategoryRepository categoryRepository;
    ProductRepository productRepository;
    ModelMapper modelMapper;


    public List<CategoryGetResponse> getAll() {
        List<CategoryEntity> categories = categoryRepository.findAll();

        return categories.stream()
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
