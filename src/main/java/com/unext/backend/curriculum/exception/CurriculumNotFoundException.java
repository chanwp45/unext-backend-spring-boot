package com.unext.backend.curriculum.exception;

import com.unext.backend.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class CurriculumNotFoundException extends AppException {
    public CurriculumNotFoundException(Long id) {
        super("CURRICULUM_NOT_FOUND", "Curriculum with id '" + id + "' was not found.", HttpStatus.NOT_FOUND);
    }
}
