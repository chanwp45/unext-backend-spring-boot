package com.unext.backend.student.exception;

import com.unext.backend.shared.exception.AppException;
import org.springframework.http.HttpStatus;

public class StudentNotFoundException extends AppException {
    public StudentNotFoundException(String studentId) {
        super("STUDENT_NOT_FOUND", "Student '" + studentId + "' was not found.", HttpStatus.NOT_FOUND);
    }
}
