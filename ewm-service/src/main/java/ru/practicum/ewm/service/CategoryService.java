package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategoryAdmin(NewCategoryDto newCategoryDto);

    CategoryDto updateCategoryAdmin(long categoryId, CategoryDto categoryDto);

    void deleteCategoryAdmin(long categoryId);

    List<CategoryDto> getAllCategoriesPublic(int from, int size);

    CategoryDto getCategoryPublic(Long catId);
}
