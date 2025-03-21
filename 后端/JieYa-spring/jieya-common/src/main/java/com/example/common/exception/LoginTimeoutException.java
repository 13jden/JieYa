package com.example.common.exception;

public class LoginTimeoutException extends RuntimeException {
    public LoginTimeoutException(String message) {
        super(message);
    }
}
