package com.raid.blog.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raid.blog.config.TestSecurityConfig;
import com.raid.blog.domain.dtos.CreateTagsRequest;
import com.raid.blog.domain.dtos.TagDto;
import com.raid.blog.domain.entities.Tag;
import com.raid.blog.mappers.TagMapper;
import com.raid.blog.services.AuthenticationService;
import com.raid.blog.services.TagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TagController.class)
@Import(TestSecurityConfig.class)
public class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private TagMapper tagMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    public void should_list_all_tags() throws Exception {
        // Arrange
        Tag tag1 = Tag.builder().name("Tag 1").build();
        Tag tag2 = Tag.builder().name("Tag 2").build();
        List<Tag> tags = List.of(tag1, tag2);

        TagDto tagDto1 = TagDto.builder().id(UUID.randomUUID()).name("Tag 1").postCount(0).build();
        TagDto tagDto2 = TagDto.builder().id(UUID.randomUUID()).name("Tag 2").postCount(0).build();
        List<TagDto> tagsDtos = List.of(tagDto1, tagDto2);
        // What to return
        given(tagService.getTags()).willReturn(tags);
        given(tagMapper.toTagResponse(tag1)).willReturn(tagDto1);
        given(tagMapper.toTagResponse(tag2)).willReturn(tagDto2);

        // Act and expect
        MvcResult mvcResult = mockMvc.perform(
                        get("/api/v1/tags")
                )
                .andExpect(status().isOk())
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        List<TagDto> retrievedTags = objectMapper.readValue(response, new TypeReference<>() {
        });
        assertNotNull(retrievedTags);
        assertEquals(2, retrievedTags.size());
        assertEquals(tagsDtos, retrievedTags);
    }

    @Test
    @WithMockUser
    public void should_create_new_tags() throws Exception {
        // Arrange
        CreateTagsRequest createTagsRequest = CreateTagsRequest
                .builder()
                .names(Set.of("New tag 1", "New tag 2"))
                .build();
        String requestJson = objectMapper.writeValueAsString(createTagsRequest);
        Tag tag1 = Tag.builder().id(UUID.randomUUID()).name("New tag 1").build();
        Tag tag2 = Tag.builder().id(UUID.randomUUID()).name("New tag 2").build();
        List<Tag> savedTags = List.of(tag1, tag2);

        TagDto tagDto1 = TagDto.builder().id(tag1.getId()).postCount(0).name("New tag 1").build();
        TagDto tagDto2 = TagDto.builder().id(tag2.getId()).postCount(0).name("New tag 2").build();
        List<TagDto> savedTagsDtos = List.of(tagDto1, tagDto2);

        // What to return
        given(tagService.createTags(createTagsRequest.getNames())).willReturn(savedTags);
        given(tagMapper.toTagResponse(tag1)).willReturn(tagDto1);
        given(tagMapper.toTagResponse(tag2)).willReturn(tagDto2 );

        // Act and expect
        MvcResult mvcResult = mockMvc
                .perform(
                        post("/api/v1/tags")
                                .contentType("application/json")
                                .content(requestJson)
                )
                .andExpect(status().isCreated())
                .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        List<TagDto> createdTagsDtos = objectMapper.readValue(response, new TypeReference<>() {
        });
        assertNotNull(createdTagsDtos);
        assertEquals(savedTagsDtos, createdTagsDtos);
    }

    @Test
    @WithMockUser
    public void should_delete_tag() throws Exception {
        // Arrange
        UUID tagId = UUID.randomUUID();

        // What to return

        // Act and expect
        mockMvc
                .perform(
                        delete("/api/v1/tags/" + tagId)
                )
                .andExpect(status().isNoContent());
    }
}
