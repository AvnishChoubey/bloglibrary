package com.blog_application.backend.exceptions;

public class ForbiddenException extends RuntimeException{
    public ForbiddenException() {
        super("Invalid email or password.");
    }
}
