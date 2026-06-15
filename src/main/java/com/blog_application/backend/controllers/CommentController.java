package com.blog_application.backend.controllers;

import com.blog_application.backend.config.CustomUserDetails;
import com.blog_application.backend.config.CustomUserDetailsService;
import com.blog_application.backend.models.User;
import com.blog_application.backend.repositories.UserRepository;
import com.blog_application.backend.requests.CommentRequest;
import com.blog_application.backend.responses.PostResponse;
import com.blog_application.backend.services.CommentService;
import com.blog_application.backend.services.JwtService;
import com.blog_application.backend.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class CommentController {

    @Autowired private CommentService commentService;
    @Autowired private PostService postService;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private CustomUserDetailsService customUserDetailsService;

    private User resolveUser(String jwt) {
        if (jwt == null || !jwtService.isTokenValid(jwt)) return null;
        return userRepository.findByEmail(jwtService.extractEmail(jwt)).orElse(null);
    }

    private void applySecurityContext(String jwt) {
        if (jwt != null && jwtService.isTokenValid(jwt)) {
            String email = jwtService.extractEmail(jwt);
            CustomUserDetails ud = (CustomUserDetails) customUserDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    private boolean isPostAuthor(User user, PostResponse post) {
        if (user == null) return false;
        return user.getEmail().equals(post.getAuthorEmail());
    }

    @PostMapping("/posts/{postId}/comments")
    public String createComment(@PathVariable Long postId,
                                @ModelAttribute CommentRequest commentRequest) {
        commentService.createComment(commentRequest, postId);
        return "redirect:/posts/" + postId;
    }

    @PutMapping("/posts/{postId}/comments/{commentId}")
    public String updateComment(@PathVariable Long postId,
                                @PathVariable Long commentId,
                                @RequestParam String content,
                                @CookieValue(name = "jwt", required = false) String jwt) {
        User user = resolveUser(jwt);
        PostResponse post = postService.getPostById(postId);
        if (!isPostAuthor(user, post)) return "redirect:/posts/" + postId;

        try {
            applySecurityContext(jwt);
            commentService.updateComment(content, postId, commentId);
        } finally {
            SecurityContextHolder.clearContext();
        }
        return "redirect:/posts/" + postId;
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public String deleteComment(@PathVariable Long postId,
                                @PathVariable Long commentId,
                                @CookieValue(name = "jwt", required = false) String jwt) {
        User user = resolveUser(jwt);
        PostResponse post = postService.getPostById(postId);
        if (!isPostAuthor(user, post)) return "redirect:/posts/" + postId;

        try {
            applySecurityContext(jwt);
            commentService.deleteComment(postId, commentId);
        } finally {
            SecurityContextHolder.clearContext();
        }
        return "redirect:/posts/" + postId;
    }
}