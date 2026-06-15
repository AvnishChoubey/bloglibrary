package com.blog_application.backend.exceptions;

public class UnauthorizedAccessException extends RuntimeException{
    public UnauthorizedAccessException() {
        super("Unauthorized operation performed.");
    }
}
