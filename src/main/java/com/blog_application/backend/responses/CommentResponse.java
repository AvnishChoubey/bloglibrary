package com.blog_application.backend.responses;

import com.blog_application.backend.models.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private String name;
    private String email;
    private String content;
    private Long post;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
