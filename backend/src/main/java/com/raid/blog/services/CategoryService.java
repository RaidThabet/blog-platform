package com.raid.blog.services;

import com.raid.blog.domain.entities.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    List<Category> listCategories();
    Category createCategory(Category category);
}
