package dev.chatree.smarthomeapi.controller.advice;


import dev.chatree.smarthomeapi.exception.BusinessException;
import dev.chatree.smarthomeapi.model.ErrorResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.IOException;

@Log4j2
@ControllerAdvice
public class ErrorAdviserController {

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Exception Error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDeniedException() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Cannot access this resource."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException() {
        String errorMessage = "Invalid path variable value. Please provide a valid enum value.";
        log.info(errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        if (e.getMessage() != null) {
            log.info("Business Exception Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "An error occurred. Please contact the administrator."));
        }
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleBrokenPipeException(IOException e) {
        if (e.getMessage().equals("java.io.IOException: Broken pipe")) {
            log.info("IOException Error: {}, {}", e.getMessage(), e.getStackTrace());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } else {
            log.error("IOException Error: {}, {}", e.getMessage(), e.getStackTrace());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    @ExceptionHandler({HttpClientErrorException.class, ResourceAccessException.class})
    public ResponseEntity<ErrorResponse> handleHttpErrorException(Exception e) {
        ErrorResponse errorResponse;
        HttpStatusCode status;

        if (e instanceof HttpClientErrorException error) {
            status = error.getStatusCode();
            if (status.equals(HttpStatus.NOT_FOUND)) {
                errorResponse = new ErrorResponse(status.value(), "Resource not found");
            } else {
                errorResponse = new ErrorResponse(status.value(), error.getMessage());
            }
        } else if (e instanceof ResourceAccessException) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorResponse = new ErrorResponse(status.value(), "Unable to connect to host");
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorResponse = new ErrorResponse(status.value(), "An unexpected error occurred");
        }
        log.info("Http Client Error Exception Error: {}", errorResponse.getErrorMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}
