package com.blog_application.backend.transformers;

import com.blog_application.backend.enums.Role;
import com.blog_application.backend.models.Comment;
import com.blog_application.backend.models.Post;
import com.blog_application.backend.models.User;
import com.blog_application.backend.requests.CommentRequest;
import com.blog_application.backend.requests.PostRequest;
import com.blog_application.backend.requests.SignupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

public class RequestToEntity {
    @Autowired private PasswordEncoder passwordEncoder;
    public static Post postRequestToPost(PostRequest postRequest) {
        Post post = Post.builder()
                .title(postRequest.getTitle())
                .content(postRequest.getContent())
                .excerpt(postRequest.getContent().substring(Math.min(200, postRequest.getContent().length())))
                .isPublished(postRequest.isPublished())
                .publishedAt(postRequest.getPublishedAt())
                .build();

        if(post.isPublished()) {
            post.setPublishedAt(LocalDateTime.now());
        } else {
            post.setPublishedAt(null);
        }

        return post;
    }

    public static Comment commentRequestToComment(CommentRequest commentRequest) {
        return Comment.builder()
                .name(commentRequest.getName())
                .email(commentRequest.getEmail())
                .content(commentRequest.getContent())
                .build();
    }

    public static User signupRequestToUser(SignupRequest signupRequest) {
        return User.builder()
                .name(signupRequest.getName())
                .email(signupRequest.getEmail())
                .password(signupRequest.getPassword())
                .role(Role.VIEWER)
                .build();
    }
}
