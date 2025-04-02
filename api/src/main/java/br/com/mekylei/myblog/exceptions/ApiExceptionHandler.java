package br.com.mekylei.myblog.exceptions;

import br.com.mekylei.myblog.dtos.auth.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

@ControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);


    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, Exception exception, String error, WebRequest request) {
        return buildResponse(status, exception, error, exception.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, Exception exception, String error, String message, WebRequest request) {
        LOGGER.error("Exception Cause: {}", exception.getMessage(), exception);

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                status.value(),
                error,
                message,
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnmappedExceptions(Exception exception, WebRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception, "An unexpected error occurred", request);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UsernameNotFoundException exception, WebRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, "User not found", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception, WebRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, exception, "Access denied", "Only logged users", request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException exception, WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, "Invalid login", "Non-existent user or invalid password", request);
    }

    @ExceptionHandler({DisabledException.class, LockedException.class})
    public ResponseEntity<ErrorResponse> handleRejectedAuthentication(AccountStatusException exception, WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, "Authentication request rejected",
                "Authentication request was rejected because the account is disabled or locked", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException exception, WebRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, "No resource found",
                "No resource found: " + exception.getResourcePath(), request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException exception, WebRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, "No resource found",
                "The resource you request was not found", request);
    }

    @ExceptionHandler({NewsNotFoundException.class, NewsNotFoundByException.class})
    public ResponseEntity<ErrorResponse> handleNewsNotFound(NewsNotFoundException exception, WebRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, "News not found", request);
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ErrorResponse> handleTokenFails(TokenException exception, WebRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception, exception.getMessage(), request);
    }

}
