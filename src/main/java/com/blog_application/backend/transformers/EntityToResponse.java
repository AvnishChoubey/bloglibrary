package com.blog_application.backend.transformers;

import com.blog_application.backend.models.Comment;
import com.blog_application.backend.models.Post;
import com.blog_application.backend.models.User;
import com.blog_application.backend.responses.CommentResponse;
import com.blog_application.backend.responses.PostResponse;
import com.blog_application.backend.responses.UserResponse;

public class EntityToResponse {
    public static PostResponse postToPostResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .content(post.getContent())
                .postTags(post.getPostTags())
                .title(post.getTitle())
                .excerpt(post.getExcerpt())
                .author(post.getAuthor().getName())
                .authorEmail(post.getAuthor().getEmail())
                .isPublished(post.isPublished())
                .publishedAt(post.getPublishedAt())
                .build();
    }

    public static CommentResponse commentToCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .name(comment.getName())
                .email(comment.getEmail())
                .post(comment.getPost().getId())
                .content(comment.getContent())
                .build();
    }

    public static UserResponse userToUserResponse(User user) {
        return UserResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .build();
    }
}
