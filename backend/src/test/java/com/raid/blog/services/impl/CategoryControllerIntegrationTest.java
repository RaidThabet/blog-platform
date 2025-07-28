package com.raid.blog.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raid.blog.domain.PostStatus;
import com.raid.blog.domain.dtos.CategoryDto;
import com.raid.blog.domain.entities.Category;
import com.raid.blog.domain.entities.Post;
import com.raid.blog.domain.entities.User;
import com.raid.blog.repositories.CategoryRepository;
import com.raid.blog.repositories.PostRepository;
import com.raid.blog.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.core.type.TypeReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        categoryRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        String encodedUserPassword = passwordEncoder.encode("password");

        User author = User.builder().name("User name").email("user@mail.com").password(encodedUserPassword).build();
        userRepository.save(author);

        Category category1 = Category.builder().name("Category 1").build();
        Category category2 = Category.builder().name("Category 2").build();
        categoryRepository.saveAll(List.of(category1, category2));

        Post post = Post.builder()
                .title("Title")
                .content("Content")
                .category(category1)
                .author(author)
                .readingTime(1)
                .status(PostStatus.PUBLISHED)
                .build();
        postRepository.save(post);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void should_list_all_categories() throws Exception {
        // Arrange
//        entityManager.flush();
//        entityManager.clear();

        // Act and assert
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        List<CategoryDto> categoryDtos = objectMapper.readValue(responseJson, new TypeReference<>() {
        });
        assertEquals(2, categoryDtos.size());
        assertEquals(categoryRepository.findCategoryByName("Category 1").getId(), categoryDtos.getFirst().getId());
        assertEquals(categoryRepository.findCategoryByName("Category 2").getId(), categoryDtos.getLast().getId());
        assertEquals("Category 1", categoryDtos.getFirst().getName());
        assertEquals("Category 2", categoryDtos.getLast().getName());
    }

    @WithMockUser
    @Test
    public void should_create_new_category_with_valid_request() throws Exception {
        // Arrange
        String createCategoryRequestJSON = "{\"name\": \"New Category\"}";

        // Act and expect
        MvcResult mvcResult = mockMvc.perform(
                        post("/api/v1/categories")
                                .contentType("application/json")
                                .content(createCategoryRequestJSON)
                )
                .andExpect(status().isCreated())
                .andReturn();
        String responseJson = mvcResult.getResponse().getContentAsString();
        CategoryDto savedCategoryDto = objectMapper.readValue(responseJson, CategoryDto.class);
        assertEquals("New Category", savedCategoryDto.getName());
        assertEquals(3L, categoryRepository.count());
    }

    @WithMockUser
    @Test
    public void should_not_create_category_with_invalid_request() throws Exception {
        // Arrange
        String createCategoryRequestJSON = "{\"name\": \" \"}";

        // Act and assert
        mockMvc.perform(
                        post("/api/v1/categories")
                                .contentType("application/json")
                                .content(createCategoryRequestJSON)
                )
                .andExpect(status().isBadRequest());
        assertEquals(2L, categoryRepository.count());
    }

    @WithMockUser
    @Test
    public void should_not_create_existing_category() throws Exception {
        // Arrange
        String createCategoryRequestJSON = "{\"name\": \"Category 1\"}";

        // Act and assert
        mockMvc.perform(
                        post("/api/v1/categories")
                                .contentType("application/json")
                                .content(createCategoryRequestJSON)
                )
                .andExpect(status().isBadRequest());
        assertEquals(2L, categoryRepository.count());
    }

    @WithMockUser
    @Test
    public void should_not_delete_category_with_posts_associated() throws Exception {
        // Arrange
        Category category = categoryRepository.getCategoryByName("Category 1");
        UUID categoryId = category.getId();

        // Act and assert
        mockMvc
                .perform(delete("/api/v1/categories/" + categoryId))
                .andExpect(status().isConflict());
        assertEquals(2L, categoryRepository.count());
        assertTrue(categoryRepository.existsByNameIgnoreCase("Category 1"));
        assertTrue(categoryRepository.existsByNameIgnoreCase("Category 2"));
    }

    @WithMockUser
    @Test
    public void should_delete_category_with_no_posts_associated() throws Exception {
        // Arrange
        Category category = categoryRepository.getCategoryByName("Category 2");
        UUID categoryId = category.getId();

        // Act and assert
        mockMvc
                .perform(delete("/api/v1/categories/" + categoryId))
                .andExpect(status().isNoContent());
        assertEquals(1L, categoryRepository.count());
        assertFalse(categoryRepository.existsByNameIgnoreCase("Category 2"));
        assertTrue(categoryRepository.existsByNameIgnoreCase("Category 1"));
    }


}
