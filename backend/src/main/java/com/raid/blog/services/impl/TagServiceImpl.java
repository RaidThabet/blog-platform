package com.raid.blog.services.impl;

import com.raid.blog.domain.entities.Post;
import com.raid.blog.domain.entities.Tag;
import com.raid.blog.repositories.TagRepository;
import com.raid.blog.services.TagService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    public List<Tag> getTags() {
        return tagRepository.findAllWithPostCount();
    }

    @Transactional
    @Override
    public List<Tag> createTags(Set<String> tagNames) {
        var existingTags = tagRepository.findByNameIn(tagNames);
        Set<String> existingTagNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        var newTags = tagNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .map(name -> Tag.builder().name(name).posts(new HashSet<>()).build())
                .toList();

        List<Tag> savedTags = new ArrayList<>();
        if (!newTags.isEmpty()) {
            savedTags = tagRepository.saveAll(newTags);
        }

        savedTags.addAll(existingTags);

        return savedTags;
    }

    @Transactional
    @Override
    public void deleteTag(UUID id) {
        tagRepository.findById(id).ifPresent(tag -> {
            Set<Post> posts = tag.getPosts();
            if (!posts.isEmpty()) {
                throw new IllegalStateException("Cannot delete tag with posts");
            }
            tagRepository.deleteById(id);
        });
    }

    @Override
    public Tag getTagById(UUID id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No tag was found"));
    }

    @Override
    public List<Tag> getTagsByIds(Set<UUID> tagIds) {
        List<Tag> foundTags = tagRepository.findAllById(tagIds);

        if (foundTags.size() != tagIds.size()) {
            throw new EntityNotFoundException("Not all specified tag IDs exist");
        }

        return foundTags;
    }
}
