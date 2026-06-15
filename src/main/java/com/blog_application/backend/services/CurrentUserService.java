package com.blog_application.backend.services;

import com.blog_application.backend.config.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public CustomUserDetails getCurrentUser() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        return (CustomUserDetails) auth.getPrincipal();
    }
}
