package com.blog_application.backend.controllers;

import com.blog_application.backend.requests.LoginRequest;
import com.blog_application.backend.requests.SignupRequest;
import com.blog_application.backend.responses.JwtResponse;
import com.blog_application.backend.services.AuthService;
import com.blog_application.backend.services.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private JwtService jwtService;

    @GetMapping("/login")
    public String loginPage(@CookieValue(name = "jwt", required = false) String jwt, Model model) {
        if (jwt != null && jwtService.isTokenValid(jwt)) {
            return "redirect:/posts";
        }
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage(@CookieValue(name = "jwt", required = false) String jwt, Model model) {
        if (jwt != null && jwtService.isTokenValid(jwt)) {
            return "redirect:/posts";
        }
        model.addAttribute("signupRequest", new SignupRequest());
        return "signup";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute LoginRequest loginRequest,
                            HttpServletResponse response, Model model) {
        try {
            JwtResponse jwtResponse = authService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
            Cookie cookie = new Cookie("jwt", jwtResponse.getToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(30 * 60 * 60);
            response.addCookie(cookie);
            return "redirect:/posts";
        } catch (Exception e) {
            model.addAttribute("error", "Invalid email or password.");
            model.addAttribute("loginRequest", loginRequest);
            return "login";
        }
    }

    @PostMapping("/signup")
    public String registerUser(@ModelAttribute SignupRequest signupRequest, Model model) {
        try {
            authService.registerUser(signupRequest);
            model.addAttribute("success", "Account created successfully. Please log in.");
            model.addAttribute("loginRequest", new LoginRequest());
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("signupRequest", signupRequest);
            return "signup";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/posts";
    }
}