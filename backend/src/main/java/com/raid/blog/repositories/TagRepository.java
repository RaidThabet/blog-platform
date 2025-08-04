package com.raid.blog.repositories;

import com.raid.blog.domain.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    @Query("select t from Tag t left join fetch t.posts")
    List<Tag> findAllWithPostCount();

    List<Tag> findByNameIn(Collection<String> names);

    int countByName(String name);

    long count();

    int countById(UUID id);
}