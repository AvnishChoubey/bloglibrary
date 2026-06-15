package com.blog_application.backend.services;

import com.blog_application.backend.config.CustomUserDetails;
import com.blog_application.backend.enums.Role;
import com.blog_application.backend.exceptions.BadRequestException;
import com.blog_application.backend.exceptions.ResourceNotFoundException;
import com.blog_application.backend.models.Post;
import com.blog_application.backend.models.PostTag;
import com.blog_application.backend.models.Tag;
import com.blog_application.backend.models.User;
import com.blog_application.backend.repositories.PostRepository;
import com.blog_application.backend.repositories.PostTagRepository;
import com.blog_application.backend.repositories.UserRepository;
import com.blog_application.backend.requests.PostRequest;
import com.blog_application.backend.responses.PostResponse;
import com.blog_application.backend.specification.PostSpecification;
import com.blog_application.backend.transformers.EntityToResponse;
import com.blog_application.backend.transformers.RequestToEntity;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {
    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PostTagRepository postTagRepository;
    @Autowired private TagService tagService;
    @Autowired private CurrentUserService currentUserService;

    //complete
    public Page<PostResponse> getAllPosts(String search, Long authorId, List<Long> tagIds, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Post> specification = PostSpecification.filterPosts(authorId, search, tagIds);

        Page<Post> postsPage = postRepository.findAll(specification, pageable);

        return postsPage.map(EntityToResponse::postToPostResponse);
    }

    //complete
    public PostResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Blog not found"));
        return EntityToResponse.postToPostResponse(post);
    }

    //complete
    public PostResponse createPost(PostRequest postRequest) {
        CustomUserDetails customUserDetails = currentUserService.getCurrentUser();
        String email = customUserDetails.getUsername();
        User user = userRepository.findByEmail(email).get();

        List<Tag> existingTags = tagService.addAll(postRequest.getTags());

        Post post = RequestToEntity.postRequestToPost(postRequest);

        for(Tag tag : existingTags) {
            PostTag postTag = new PostTag();
            postTag.setPost(post);
            postTag.setTag(tag);
            PostTag savedPostTag = postTagRepository.save(postTag);
            post.getPostTags().add(savedPostTag);
            tag.getPostTags().add(savedPostTag);
        }

        if(user.getRole().equals(Role.AUTHOR)) {
            post.setAuthor(user);
        } else {
            User authorByAdmin = userRepository.findById(postRequest.getAuthorId()).orElseThrow(() -> new BadRequestException("Invalid author requested"));
            post.setAuthor(authorByAdmin);
        }

        return EntityToResponse.postToPostResponse(postRepository.save(post));
    }

    //complete
    @Transactional
    public PostResponse updatePost(PostRequest postRequest, Long postId) {
        CustomUserDetails customUserDetails = currentUserService.getCurrentUser();
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Blog not found."));

        String email = customUserDetails.getUsername();
        User user = userRepository.findByEmail(email).get();


        if(post.getAuthor().getEmail().equals(email) || user.getRole().equals(Role.ADMIN)) {
            if(postRequest.getContent() != null) {
                post.setContent(postRequest.getContent());
            }

            if(postRequest.getTitle() != null) {
                post.setTitle(postRequest.getTitle());
            }

            if(postRequest.isPublished() != post.isPublished()) {
                post.setPublished(postRequest.isPublished());
            }

            if(postRequest.isPublished()) {
                if(postRequest.getPublishedAt() != null) post.setPublishedAt(postRequest.getPublishedAt());
                else post.setPublishedAt(LocalDateTime.now());
            }

            if(postRequest.getTags() != null) {
                List<Tag> existingTags = tagService.addAll(postRequest.getTags());

                for (Tag tag : existingTags) {
                    PostTag postTag = new PostTag();
                    postTag.setPost(post);
                    postTag.setTag(tag);

                    post.getPostTags().add(postTag);
                }
            }
        }

        if(user.getRole().equals(Role.ADMIN) && postRequest.getAuthorId() != null) {
            User author = userRepository.findById(postRequest.getAuthorId()).orElseThrow(() -> new BadRequestException("Invalid Author Id."));
            post.setAuthor(author);
        }

        return EntityToResponse.postToPostResponse(post);
    }

    //complete
    public void deletePost(Long postId) {
        CustomUserDetails customUserDetails = currentUserService.getCurrentUser();
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Blog not found."));

        String email = customUserDetails.getUsername();
        User user = userRepository.findByEmail(email).get();

        if(user.getRole().equals(Role.ADMIN) || (user.getId().equals(post.getAuthor().getId()))) {
            postRepository.deleteById(postId);
        } else {
            throw new RuntimeException("User unauthorized. Operation denied.");
        }
    }
}
