package com.blog_application.backend.apis;

import com.blog_application.backend.requests.UserRoleChangeRequest;
import com.blog_application.backend.responses.UserResponse;
import com.blog_application.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("apiUserController")
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired private UserService userService;

    @PutMapping("/change-role")
    public ResponseEntity<UserResponse> changeRole(@RequestBody UserRoleChangeRequest userRoleChangeRequest) {
        UserResponse userResponse = userService.changeRole(userRoleChangeRequest.getEmail(), userRoleChangeRequest.getRole());
        return ResponseEntity.ok(userResponse);
    }
}
