package com.raid.blog.services.impl;

import com.raid.blog.domain.entities.Category;
import com.raid.blog.domain.entities.Post;
import com.raid.blog.repositories.CategoryRepository;
import com.raid.blog.services.CategoryService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CategoryServiceImplIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void should_create_and_list_category() {
        Category category = new Category();
        category.setName("Science");

        categoryService.createCategory(category);

        List<Category> categories = categoryService.listCategories();
        assertFalse(categories.isEmpty());
        assertEquals("Science", categories.getFirst().getName());
    }

    @Test
    void should_not_allow_category_duplication() {
        Category category = new Category();
        category.setName("Science");
        Category duplicate = new Category();
        duplicate.setName("Science");

        categoryService.createCategory(category);

        Exception exception = assertThrows(IllegalStateException.class, () -> categoryService.createCategory(duplicate));
        assertEquals("Category already exists with name Science", exception.getMessage());

        List<Category> categories = categoryService.listCategories();
        assertEquals(1, categories.size());
        assertEquals(category.getName(), categories.getFirst().getName());
    }

    @Test
    void should_not_delete_category_with_posts_and_should_delete_after_posts_removed() {
        Post post = new Post();
        Category category = new Category();
        category.setName("Gaming");
        post.setCategory(category);
        category.setPosts(List.of(post));

        Category persistedCategoryWithPost = categoryService.createCategory(category); // persist to get ID

        Exception exp = assertThrows(IllegalStateException.class, () -> categoryService.deleteCategory(persistedCategoryWithPost.getId()));

        assertEquals("Category has posts associated with it", exp.getMessage());

        persistedCategoryWithPost.setPosts(Collections.emptyList());

        Category persistedCategoryWithNoPosts = categoryRepository.save(persistedCategoryWithPost);

        categoryService.deleteCategory(persistedCategoryWithNoPosts.getId());
        List<Category> categories = categoryService.listCategories();
        assertEquals(0, categories.size());
    }

}
