package com.raid.blog.services.impl;

import com.raid.blog.domain.entities.Category;
import com.raid.blog.repositories.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void should_create_category_when_name_does_not_exist() {
        // Arrange
        Category category = new Category();
        category.setName("Technology");

        // What to return
        when(categoryRepository.existsByNameIgnoreCase("Technology")).thenReturn(false);
        when(categoryRepository.save(category)).thenReturn(category);

        // Act
        Category result = categoryService.createCategory(category);

        // Assert
        assertEquals(category, result);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void should_throw_exception_when_creating_duplicate_category() {
        Category category = new Category();
        category.setName("Science");
        when(categoryRepository.existsByNameIgnoreCase("Science")).thenReturn(false);
        when(categoryRepository.save(category)).thenReturn(category);
        categoryService.createCategory(category);

        Category duplicate = new Category();
        duplicate.setName("Science");
        when(categoryRepository.existsByNameIgnoreCase("Science")).thenReturn(true);

        Exception exception = assertThrows(IllegalStateException.class, () -> categoryService.createCategory(duplicate));
        assertEquals("Category already exists with name Science", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_delete_existing_category_with_no_posts_associated_to_it() {

        // Arrange
        UUID categoryId = UUID.randomUUID();
        Category category = new Category();
        category.setId(categoryId);
        category.setPosts(Mockito.mock(List.class));

        // What to return
        Optional<Category> cat = Optional.of(category);
        when(categoryRepository.findById(categoryId)).thenReturn(cat);
        when(cat.get().getPosts().isEmpty()).thenReturn(true);

        // Act
        categoryService.deleteCategory(categoryId);

        // Assert
        verify(categoryRepository, times(1)).deleteById(categoryId);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_throw_exception_when_deleting_category_associated_to_posts() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category();
        category.setId(categoryId);
        category.setPosts(Mockito.mock(List.class));

        Optional<Category> cat = Optional.of(category);
        when(categoryRepository.findById(categoryId)).thenReturn(cat);
        when(cat.get().getPosts().isEmpty()).thenReturn(false);

        Exception exception = assertThrows(IllegalStateException.class, () -> categoryService.deleteCategory(categoryId));

        assertEquals("Category has posts associated with it", exception.getMessage());
    }
}