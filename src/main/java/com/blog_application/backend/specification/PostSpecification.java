package com.blog_application.backend.specification;

import com.blog_application.backend.models.Post;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class PostSpecification {

    public static Specification<Post> filterPosts(Long authorId, String search, List<Long> tagIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("isPublished"), true));

            if (authorId != null) {
                predicates.add(
                        cb.equal(root.get("author"), authorId)
                );
            }

            if (search != null && !search.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"));

//                predicates.add(cb.like(cb.lower(root.get("author")), "%" + search.toLowerCase() + "%"));

                predicates.add(cb.like(cb.lower(root.get("content")), "%" + search.toLowerCase() + "%"));

//                predicates.add(cb.like(cb.lower(root.get("post_tags")), "%" + search.toLowerCase() + "%"));
            }

            if (tagIds != null && !tagIds.isEmpty()) {
                for(Long tagId : tagIds) {
                    predicates.add(
                            cb.equal(root.get("tagId"), tagId)
                    );
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
