package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.NewCategoryDto;
import ru.practicum.ewm.entity.Category;
import ru.practicum.ewm.exception.*;
import ru.practicum.ewm.mapper.CategoryMapper;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    @Override
    @Transactional
    public CategoryDto createCategoryAdmin(NewCategoryDto newCategoryDto) {
        if (newCategoryDto.getName().isBlank()) {
            throw new ValidationException("Название категории не может быть пустым");
        }
        Optional<Category> categoryFromRep = categoryRepository.findByName(newCategoryDto.getName());
        if (categoryFromRep.isPresent()) {
            throw new DataIsNotCorrectException("Категория уже есть");
        }
        return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.toCategory(newCategoryDto)));
    }

    @Override
    @Transactional
    public CategoryDto updateCategoryAdmin(long categoryId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ObjectNotFoundException("Нет данной категории"));
        Optional<Category> categoryFromRep = categoryRepository.findByName(categoryDto.getName());
        if (categoryFromRep.isPresent() && !categoryFromRep.get().getId().equals(categoryId)) {
            throw new ObjectAlreadyExistsException("Категория с таким названием уже есть");
        }
        if (categoryDto.getName().length() > 50) {
            throw new ValidationException("Очень длинное название");
        }
        category.setName(categoryDto.getName());
        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    @Transactional
    public void deleteCategoryAdmin(long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ObjectNotFoundException("Category not found"));
        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ObjectContainsDataException("STOP! Категории с событиями не удаляем.");
        }
        categoryRepository.deleteById(categoryId);
    }

    @Override
    public List<CategoryDto> getAllCategoriesPublic(int from, int size) {
        Pageable pageable = PageRequest.of(from > 0 ? from / size : 0, size,
                Sort.by("id").ascending());
        List<Category> categories = categoryRepository.findAll(pageable).toList();
        if (categories.isEmpty()) {
            return Collections.emptyList();
        }
        return categories.stream().map(CategoryMapper::toCategoryDto).collect(toList());
    }

    @Override
    public CategoryDto getCategoryPublic(Long catId) {
        final Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new ObjectNotFoundException("Category not found"));
        return CategoryMapper.toCategoryDto(category);
    }
}
