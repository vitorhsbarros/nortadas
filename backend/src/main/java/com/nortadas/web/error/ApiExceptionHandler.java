package com.nortadas.web.error;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates web-layer exceptions into RFC-7807 {@link ProblemDetail} responses
 * for the beach API. Kept as a single central advice so further handlers (e.g. a
 * {@code 404} for an unknown beach in US012/#17) can be added alongside this one.
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
}
