package com.raid.blog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raid.blog.domain.dtos.CategoryDto;
import com.raid.blog.domain.dtos.CreateCategoryRequest;
import com.raid.blog.domain.entities.Category;
import com.raid.blog.mappers.CategoryMapper;
import com.raid.blog.security.JwtAuthenticationFilter;
import com.raid.blog.services.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import(CategoryControllerTest.TestSecurityConfig.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CategoryMapper categoryMapper;

    @WithMockUser(username = "user")
    @Test
    void should_return_list_of_categories() throws Exception {
        // Arrange
        UUID category1Id = UUID.randomUUID();
        UUID category2Id = UUID.randomUUID();
        Category category1 = Category.builder().id(category1Id).name("Category 1").build();
        Category category2 = Category.builder().id(category2Id).name("Category 2").build();


        List<Category> categories = List.of(category1, category2);

        // What to return
        given(categoryService.listCategories()).willReturn(categories);
        given(categoryMapper.toDto(categories.getFirst())).willReturn(CategoryDto.builder().id(category1Id).name("Category 1").postCount(0).build());
        given(categoryMapper.toDto(categories.getLast())).willReturn(CategoryDto.builder().id(category2Id).name("Category 2").postCount(0).build());

        // Act and expect
        mockMvc.perform(get("/api/v1/categories"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(category1Id.toString()))
                .andExpect(jsonPath("$[1].id").value(category2Id.toString()))
                .andExpect(jsonPath("$[0].name").value("Category 1"))
                .andExpect(jsonPath("$[1].name").value("Category 2"));
    }

    @WithMockUser
    @Test
    void should_create_category_with_valid_request() throws Exception {
        // Arrange
        CreateCategoryRequest createCategoryRequest = CreateCategoryRequest.builder().name("Category").build();
        Category categoryToCreate = Category.builder().name("Category").build();
        Category savedCategory = Category.builder().id(UUID.randomUUID()).name("Category").build();
        CategoryDto categoryDto = CategoryDto.builder().id(savedCategory.getId()).name("Category").postCount(0).build();
        String createCategoryRequestJSON = "{\"name\": \"Category\"}";

        // What to return
        given(categoryMapper.toEntity(createCategoryRequest)).willReturn(categoryToCreate);
        given(categoryService.createCategory(categoryToCreate)).willReturn(savedCategory);
        given(categoryMapper.toDto(savedCategory)).willReturn(categoryDto);

        // Act and expect
        MvcResult result = mockMvc.perform(post("/api/v1/categories")
                .contentType("application/json")
                .content(createCategoryRequestJSON)
        )
                .andExpect(status().isCreated())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        System.out.println(responseBody);
        CategoryDto response = objectMapper.readValue(responseBody, CategoryDto.class);
        assertEquals("Category", response.getName());
        assertEquals(0, response.getPostCount());
    }

    @Test
    public void should_delete_category() throws Exception {
        // Arrange
        UUID categoryId = UUID.randomUUID();

        // Act and expect
        mockMvc.perform(delete("/api/v1/categories/" + categoryId))
                .andExpect(status().isNoContent());
    }

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll()
                    )
                    .csrf(AbstractHttpConfigurer::disable)
                    .build();
        }
    }
}