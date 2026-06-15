package com.blog_application.backend.apis;

import com.blog_application.backend.config.CustomUserDetails;
import com.blog_application.backend.requests.PostRequest;
import com.blog_application.backend.responses.PostResponse;
import com.blog_application.backend.services.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("apiPostController")
@RequestMapping("/api/v1/posts")
public class PostController {
    @Autowired private PostService postService;

    //complete
    @GetMapping("/")
    public ResponseEntity<Page<PostResponse>> getAllPosts(@RequestParam(required = false) String search,
                              @RequestParam(required = false) Long authorId,
                              @RequestParam(required = false) List<Long> tagIds,
                              @RequestParam(required = false, defaultValue = "0") int page,
                              @RequestParam(required = false, defaultValue = "10") int size,
                              @RequestParam(required = false, defaultValue = "id") String sortBy,
                              @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
        Page<PostResponse> postResponsesPage = postService.getAllPosts(search, authorId, tagIds, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(postResponsesPage);
    }

    //complete
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable("postId") Long postId) {
        PostResponse postResponse = postService.getPostById(postId);
        System.out.println(postResponse.toString());
        return ResponseEntity.ok(postResponse);
    }

    //complete
    @PostMapping("/create")
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest postRequest) {
        PostResponse postResponse = postService.createPost(postRequest);
        return ResponseEntity.ok(postResponse);
    }

    //complete
    @PutMapping("/{postId}/update")
    public ResponseEntity<PostResponse> updatePost(@Valid @RequestBody PostRequest postRequest,
                                                   @PathVariable("postId") Long postId) {
        PostResponse postResponse = postService.updatePost(postRequest, postId);
        return ResponseEntity.ok(postResponse);
    }

    //complete
    @DeleteMapping("/{postId}/delete")
    public ResponseEntity<Void> deletePost(@PathVariable("postId") Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok().build();
    }
}
