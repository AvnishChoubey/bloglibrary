package com.blog_application.backend.controllers;

import com.blog_application.backend.config.CustomUserDetails;
import com.blog_application.backend.config.CustomUserDetailsService;
import com.blog_application.backend.enums.Role;
import com.blog_application.backend.models.User;
import com.blog_application.backend.repositories.UserRepository;
import com.blog_application.backend.requests.CommentRequest;
import com.blog_application.backend.requests.PostRequest;
import com.blog_application.backend.responses.CommentResponse;
import com.blog_application.backend.responses.PostResponse;
import com.blog_application.backend.services.CommentService;
import com.blog_application.backend.services.JwtService;
import com.blog_application.backend.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PostController {

    @Autowired private PostService postService;
    @Autowired private CommentService commentService;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;
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

    private void addCurrentUserToModel(User user, Model model) {
        if (user != null) {
            model.addAttribute("currentUserEmail", user.getEmail());
            model.addAttribute("currentUserName", user.getName());
            model.addAttribute("currentUserRole", user.getRole().name());
            model.addAttribute("currentUserId", user.getId());
        }
    }

    private boolean canManagePost(User user, PostResponse post) {
        if (user == null) return false;
        return user.getRole() == Role.ADMIN || user.getEmail().equals(post.getAuthorEmail());
    }

    @GetMapping("/posts")
    public String getAllPosts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "publishedAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @CookieValue(name = "jwt", required = false) String jwt,
            Model model) {

        Page<PostResponse> postsPage = postService.getAllPosts(search, authorId, tagIds, page, size, sortBy, sortDirection);
        List<User> authors = userRepository.findByRoleIn(List.of(Role.AUTHOR, Role.ADMIN));

        model.addAttribute("postsPage", postsPage);
        model.addAttribute("authors", authors);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentAuthorId", authorId);
        model.addAttribute("currentTagIds", tagIds);
        model.addAttribute("currentSortBy", sortBy);
        model.addAttribute("currentSortDirection", sortDirection);
        addCurrentUserToModel(resolveUser(jwt), model);

        return "posts";
    }

    @GetMapping("/posts/{postId}")
    public String getPost(@PathVariable Long postId,
                          @CookieValue(name = "jwt", required = false) String jwt,
                          Model model) {
        PostResponse post = postService.getPostById(postId);
        List<CommentResponse> comments = commentService.getAllComments(postId);
        User currentUser = resolveUser(jwt);

        boolean isPostAuthor = currentUser != null && currentUser.getEmail().equals(post.getAuthorEmail());

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("commentRequest", new CommentRequest());
        model.addAttribute("canManagePost", canManagePost(currentUser, post));
        model.addAttribute("isPostAuthor", isPostAuthor);
        addCurrentUserToModel(currentUser, model);

        return "post";
    }

    @GetMapping("/posts/create")
    public String createPostPage(@CookieValue(name = "jwt", required = false) String jwt,
                                 Model model) {
        User user = resolveUser(jwt);
        if (user == null) return "redirect:/login";
        if (user.getRole() == Role.VIEWER) return "redirect:/posts";

        model.addAttribute("postRequest", new PostRequest());
        addCurrentUserToModel(user, model);
        if (user.getRole() == Role.ADMIN) {
            model.addAttribute("authors", userRepository.findByRoleIn(List.of(Role.AUTHOR, Role.ADMIN)));
        }
        return "create-post-form";
    }

    @PostMapping("/posts/create")
    public String createPost(@ModelAttribute PostRequest postRequest,
                             @CookieValue(name = "jwt", required = false) String jwt,
                             Model model) {
        User user = resolveUser(jwt);
        if (user == null) return "redirect:/login";
        if (user.getRole() == Role.VIEWER) return "redirect:/posts";

        if (user.getRole() == Role.AUTHOR) {
            postRequest.setAuthorId(user.getId());
        }

        try {
            applySecurityContext(jwt);
            PostResponse created = postService.createPost(postRequest);
            SecurityContextHolder.clearContext();
            return "redirect:/posts/" + created.getId();
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("postRequest", postRequest);
            addCurrentUserToModel(user, model);
            if (user.getRole() == Role.ADMIN) {
                model.addAttribute("authors", userRepository.findByRoleIn(List.of(Role.AUTHOR, Role.ADMIN)));
            }
            return "create-post-form";
        }
    }

    @GetMapping("/posts/{postId}/edit")
    public String editPostPage(@PathVariable Long postId,
                               @CookieValue(name = "jwt", required = false) String jwt,
                               Model model) {
        User user = resolveUser(jwt);
        if (user == null) return "redirect:/login";

        PostResponse post = postService.getPostById(postId);
        if (!canManagePost(user, post)) return "redirect:/posts/" + postId;

        String tagsStr = post.getPostTags() == null ? "" :
                post.getPostTags().stream()
                        .map(pt -> pt.getTag().getName())
                        .collect(Collectors.joining(" "));

        PostRequest postRequest = PostRequest.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .isPublished(post.isPublished())
                .publishedAt(post.getPublishedAt())
                .tags(tagsStr)
                .authorId(user.getRole() == Role.ADMIN ? null : user.getId())
                .build();

        model.addAttribute("postRequest", postRequest);
        model.addAttribute("postId", postId);
        model.addAttribute("post", post);
        addCurrentUserToModel(user, model);
        if (user.getRole() == Role.ADMIN) {
            model.addAttribute("authors", userRepository.findByRoleIn(List.of(Role.AUTHOR, Role.ADMIN)));
        }
        return "edit-post-form";
    }

    @PutMapping("/posts/{postId}/edit")
    public String updatePost(@PathVariable Long postId,
                             @ModelAttribute PostRequest postRequest,
                             @CookieValue(name = "jwt", required = false) String jwt,
                             Model model) {
        User user = resolveUser(jwt);
        if (user == null) return "redirect:/login";

        PostResponse post = postService.getPostById(postId);
        if (!canManagePost(user, post)) return "redirect:/posts/" + postId;

        if (user.getRole() == Role.AUTHOR) {
            postRequest.setAuthorId(user.getId());
        }

        try {
            applySecurityContext(jwt);
            postService.updatePost(postRequest, postId);
            SecurityContextHolder.clearContext();
            return "redirect:/posts/" + postId;
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            model.addAttribute("error", e.getMessage());
            model.addAttribute("postRequest", postRequest);
            model.addAttribute("postId", postId);
            model.addAttribute("post", post);
            addCurrentUserToModel(user, model);
            if (user.getRole() == Role.ADMIN) {
                model.addAttribute("authors", userRepository.findByRoleIn(List.of(Role.AUTHOR, Role.ADMIN)));
            }
            return "edit-post-form";
        }
    }

    @DeleteMapping("/posts/{postId}/delete")
    public String deletePost(@PathVariable Long postId,
                             @CookieValue(name = "jwt", required = false) String jwt) {
        User user = resolveUser(jwt);
        if (user == null) return "redirect:/login";

        PostResponse post = postService.getPostById(postId);
        if (!canManagePost(user, post)) return "redirect:/posts/" + postId;

        try {
            applySecurityContext(jwt);
            postService.deletePost(postId);
        } finally {
            SecurityContextHolder.clearContext();
        }
        return "redirect:/posts";
    }
}