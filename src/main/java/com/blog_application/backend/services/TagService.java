package com.blog_application.backend.services;

import com.blog_application.backend.models.Tag;
import com.blog_application.backend.repositories.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TagService {
    @Autowired private TagRepository tagRepository;

    public List<Tag> addAll(Collection<String> tagNames) {
        Set<String> normalizedNames = new HashSet<>();

        tagNames.forEach(tagName -> {
            normalizedNames.add(tagName.trim().toLowerCase());
        });

        List<Tag> existingTags = tagRepository.findByNameIn(tagNames);

        List<Tag> newTags = existingTags.stream()
                .filter(name -> !existingTags.contains(name))
                .map(name -> {
                    Tag tag = new Tag();
                    tag.setName(name.toString());
                    return tag;
                })
                .toList();

        if (!newTags.isEmpty()) {
            List<Tag> savedTags = tagRepository.saveAll(newTags);
            System.out.println("NEW TAGS SAVED");
            existingTags.addAll(savedTags);
        }

        return existingTags;
    }
}