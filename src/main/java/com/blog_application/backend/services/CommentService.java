package com.blog_application.backend.services;

import com.blog_application.backend.config.CustomUserDetails;
import com.blog_application.backend.exceptions.ResourceNotFoundException;
import com.blog_application.backend.exceptions.UnauthorizedAccessException;
import com.blog_application.backend.models.Comment;
import com.blog_application.backend.models.Post;
import com.blog_application.backend.repositories.CommentRepository;
import com.blog_application.backend.repositories.PostRepository;
import com.blog_application.backend.requests.CommentRequest;
import com.blog_application.backend.responses.CommentResponse;
import com.blog_application.backend.transformers.EntityToResponse;
import com.blog_application.backend.transformers.RequestToEntity;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {
    @Autowired private CommentRepository commentRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private CurrentUserService currentUserService;

    public List<CommentResponse> getAllComments(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Blog not found."));
        List<Comment> comments = commentRepository.findByPost(post);
        List<CommentResponse> commentResponses = new ArrayList<>();
        comments.forEach(comment -> commentResponses.add(EntityToResponse.commentToCommentResponse(comment)));
        return commentResponses;
    }

    public CommentResponse createComment(CommentRequest commentRequest, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Blog not found."));
        Comment comment = RequestToEntity.commentRequestToComment(commentRequest);

        comment.setPost(post);
        Comment savedComment = commentRepository.save(comment);
        return EntityToResponse.commentToCommentResponse(savedComment);
    }

    @Transactional
    public CommentResponse updateComment(String newContent, Long postId, Long commentId) {
        CustomUserDetails customUserDetails = currentUserService.getCurrentUser();
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Blog not found."));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment not found."));

        if(!comment.getPost().getId().equals(post.getId())) {
            throw new ResourceNotFoundException("Comment not found.");
        }

        if(comment.getPost().getAuthor().getEmail().equals(customUserDetails.getUsername())) {
            comment.setContent(newContent);
            return EntityToResponse.commentToCommentResponse(comment);
        } else {
            throw new UnauthorizedAccessException();
        }
    }

    public void deleteComment(Long postId, Long commentId) {
        CustomUserDetails customUserDetails = currentUserService.getCurrentUser();
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Blog not found."));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment not found."));

        if(!comment.getPost().getId().equals(post.getId())) {
         throw new ResourceNotFoundException("Comment not found.");
        }

        if(comment.getPost().getAuthor().getEmail().equals(customUserDetails.getUsername())) {
         commentRepository.deleteById(commentId);
        } else {
         throw new UnauthorizedAccessException();
        }
    }
}
