/**
 * Web-layer error handling — the {@code @RestControllerAdvice} and the exceptions
 * it translates into RFC-7807 {@code ProblemDetail} responses for the beach API
 * (docs/architecture.md §5). Keeps HTTP error mapping out of the controllers.
 */
package com.nortadas.web.error;
