package com.hasta.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String CODE = "code";
    private static final String MESSAGE = "message";
    private static final String TIMESTAMP = "timestamp";
    private static final String PATH = "path";

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Map<String, String>> defaultErrorHandler(
            HttpServletRequest req,
            ApplicationException e
    ) {
        Map<String, String> body = buildErrorBody(req, e);
        HttpStatus status = HttpStatus.valueOf(e.getHttpStatusCode());
        return new ResponseEntity<>(body, status);
    }

    private static Map<String, String> buildErrorBody(HttpServletRequest req, ApplicationException e) {
        Map<String, String> body = new HashMap<>();
        body.put(CODE, e.getCode());
        body.put(MESSAGE, e.getMessage() != null ? e.getMessage() : "");
        body.put(TIMESTAMP, String.valueOf(e.getTimestamp()));
        body.put(PATH, formatPath(req));
        return body;
    }

    private static String formatPath(HttpServletRequest req) {
        return req.getMethod() + " " + req.getServletPath();
    }
}