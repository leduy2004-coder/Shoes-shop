package com.java.shoes_service.service;

import com.java.shoes_service.dto.category.CategoryGetResponse;
import com.java.shoes_service.dto.category.CategoryResponse;
import com.java.shoes_service.entity.brand.CategoryEntity;
import com.java.shoes_service.repository.CategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {

    CategoryRepository categoryRepository;
    ModelMapper modelMapper;

    public CategoryGetResponse getChildrenByParentId(String parentId) {
        CategoryEntity parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + parentId));

        // map parent
        CategoryGetResponse dto = modelMapper.map(parent, CategoryGetResponse.class);

        // map children
        List<CategoryResponse> children = categoryRepository.findByParentId(parentId).stream()
                .map(e -> modelMapper.map(e, CategoryResponse.class))
                .toList();

        dto.setChildren(children);
        return dto;
    }


    public List<CategoryGetResponse> getParentsWithChildren() {
        // Lấy tất cả cha
        List<CategoryEntity> roots = categoryRepository.findByParentIdIsNull();
        if (roots.isEmpty()) return List.of();

        // Lấy tất cả children của các cha (1 lần)
        List<String> rootIds = roots.stream().map(CategoryEntity::getId).toList();
        List<CategoryEntity> allChildren = categoryRepository.findByParentIdIn(rootIds);

        // Group children theo parentId
        Map<String, List<CategoryResponse>> childrenMap = allChildren.stream()
                .map(ch -> modelMapper.map(ch, CategoryResponse.class))
                .collect(Collectors.groupingBy(CategoryResponse::getParentId));

        // Map cha -> DTO và gắn children
        return roots.stream()
                .map(root -> {
                    CategoryGetResponse dto = modelMapper.map(root, CategoryGetResponse.class);
                    dto.setChildren(childrenMap.getOrDefault(root.getId(), List.of()));
                    return dto;
                })
                .toList();
    }

    public List<CategoryResponse> getOnlyParents() {
        return categoryRepository.findByParentIdIsNull()
                .stream()
                .map(e -> modelMapper.map(e, CategoryResponse.class))
                .toList();
    }


}
