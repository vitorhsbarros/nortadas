package com.nortadas.web.error;

import com.nortadas.application.usecase.BeachNotFoundException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates web-layer and use-case exceptions into RFC-7807 {@link ProblemDetail}
 * responses for the beach API. Kept as a single central advice so all HTTP error
 * mappings for the beach endpoints live in one place.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidPaginationException.class)
    ProblemDetail handleInvalidPagination(InvalidPaginationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid pagination parameters");
        problem.setType(URI.create("https://api.nortada.pt/problems/invalid-pagination"));
        return problem;
    }

    @ExceptionHandler(BeachNotFoundException.class)
    ProblemDetail handleBeachNotFound(BeachNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Beach not found");
        problem.setType(URI.create("https://api.nortada.pt/problems/beach-not-found"));
        return problem;
    }
}
