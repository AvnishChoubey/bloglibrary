package com.blog_application.backend.responses;

import com.blog_application.backend.models.PostTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String excerpt;
    private String content;
    private String author;
    private String authorEmail;
    private boolean isPublished;
    private LocalDateTime publishedAt;
    private Set<PostTag> postTags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
