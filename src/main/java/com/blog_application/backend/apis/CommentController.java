package com.blog_application.backend.apis;

import com.blog_application.backend.config.CustomUserDetails;
import com.blog_application.backend.requests.CommentRequest;
import com.blog_application.backend.responses.CommentResponse;
import com.blog_application.backend.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("apiCommentController")
@RequestMapping("/api/v1/posts/{postId}/comments")
public class CommentController {
    @Autowired private CommentService commentService;

    @GetMapping("/")
    public ResponseEntity<List<CommentResponse>> getAllComments(@PathVariable("postId") Long postId) {
        List<CommentResponse> commentResponseList = commentService.getAllComments(postId);
        return ResponseEntity.ok(commentResponseList);
    }

    @PostMapping("/create")
    public ResponseEntity<CommentResponse> createComment(@RequestBody CommentRequest commentRequest,
                                                         @PathVariable("postId") Long postId) {
       CommentResponse commentResponse = commentService.createComment(commentRequest, postId);
       return ResponseEntity.ok(commentResponse);
    }

    @PutMapping("/{commentId}/update")
    public ResponseEntity<CommentResponse> updateComment(@RequestBody String newContent,
                                                         @PathVariable("postId") Long postId,
                                                         @PathVariable("commentId") Long commentId) {
        CommentResponse commentResponse = commentService.updateComment(newContent, postId, commentId);
        return ResponseEntity.ok(commentResponse);
    }

    @DeleteMapping("/{commentId}/delete")
    public String deleteComment(@PathVariable("postId") Long postId,
                                @PathVariable("commentId") Long commentId) {
        commentService.deleteComment(postId, commentId);
        return "post";
    }
}
