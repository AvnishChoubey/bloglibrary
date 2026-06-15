package com.blog_application.backend.services;

import com.blog_application.backend.enums.Role;
import com.blog_application.backend.exceptions.BadRequestException;
import com.blog_application.backend.models.User;
import com.blog_application.backend.repositories.UserRepository;
import com.blog_application.backend.responses.UserResponse;
import com.blog_application.backend.transformers.EntityToResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserResponse changeRole(String email, String roleName) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new BadRequestException("Invalid user requested."));
        if(roleName.equals("AUTHOR")) {
            user.setRole(Role.AUTHOR);
        }
        if(roleName.equals("ADMIN")) {
            user.setRole(Role.ADMIN);
        }

        return EntityToResponse.userToUserResponse(user);
    }
}
