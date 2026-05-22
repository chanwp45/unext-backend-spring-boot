package com.unext.backend.user.exception;

import com.unext.backend.shared.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UserNotFoundException extends AppException {

    public UserNotFoundException(UUID id) {
        super("USER_NOT_FOUND", "User with id '" + id + "' was not found.", HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(String email) {
        super("USER_NOT_FOUND", "User with email '" + email + "' was not found.", HttpStatus.NOT_FOUND);
    }
}
