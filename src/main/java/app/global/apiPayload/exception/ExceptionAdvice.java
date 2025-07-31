package app.global.apiPayload.exception;

import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.code.ErrorReasonDTO;
import app.global.apiPayload.code.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            String fieldName = fieldError.getField();
            String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
            errors.merge(fieldName, errorMessage, (existing, newMsg) -> existing + ", " + newMsg);
        });


        return handleExceptionInternalArgs(e, HttpHeaders.EMPTY, ErrorStatus._BAD_REQUEST, request, errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnknownException(Exception e, HttpServletRequest request, WebRequest webRequest) {
        return handleExceptionInternalFalse(e, ErrorStatus._INTERNAL_SERVER_ERROR, HttpHeaders.EMPTY,
                ErrorStatus._INTERNAL_SERVER_ERROR.getHttpStatus(), webRequest, e.getMessage());
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<Object> onThrowException(GeneralException ex, HttpServletRequest request) {
        ErrorReasonDTO errorReason = ex.getErrorReasonHttpStatus();
        return handleExceptionInternal(ex, errorReason, null, request);
    }

    private ResponseEntity<Object> handleExceptionInternal(Exception e, ErrorReasonDTO reason,
                                                           HttpHeaders headers, HttpServletRequest request) {
        ApiResponse<Object> body = ApiResponse.onFailure(reason.getCode(), reason.getMessage(), null);
        WebRequest webRequest = new ServletWebRequest(request);
        return super.handleExceptionInternal(e, body, headers, reason.getHttpStatus(), webRequest);
    }

    private ResponseEntity<Object> handleExceptionInternalFalse(Exception e, ErrorStatus status,
                                                                HttpHeaders headers, HttpStatus httpStatus,
                                                                WebRequest request, String errorPoint) {
        ApiResponse<Object> body = ApiResponse.onFailure(status.getCode(), status.getMessage(), errorPoint);
        return super.handleExceptionInternal(e, body, headers, httpStatus, request);
    }

    private ResponseEntity<Object> handleExceptionInternalArgs(Exception e, HttpHeaders headers, ErrorStatus status,
                                                               WebRequest request, Map<String, String> errorArgs) {
        ApiResponse<Object> body = ApiResponse.onFailure(status.getCode(), status.getMessage(), errorArgs);
        return super.handleExceptionInternal(e, body, headers, status.getHttpStatus(), request);
    }
}
