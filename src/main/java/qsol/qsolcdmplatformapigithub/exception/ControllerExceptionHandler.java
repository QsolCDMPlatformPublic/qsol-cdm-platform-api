package qsol.qsolcdmplatformapigithub.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import qsol.qsolcdmplatformapigithub.dto.response.ErrorResponse;

import java.util.HashMap;

@Slf4j
@RestControllerAdvice(basePackages = "qsol.qsolcdmplatformapigithub.controller")
public class ControllerExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> globalException(GlobalException e) {

        log.error(e.getMessage());

        int statusCode = e.getStatusCode();
        String message = e.getMessage();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(message)
                .statusCode(statusCode)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validateException(MethodArgumentNotValidException e) {

        BindingResult bindingResult = e.getBindingResult();

        HashMap<String, String> field = new HashMap<>();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            field.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        HttpStatus statusCode = HttpStatus.BAD_REQUEST;

        ErrorResponse errorResponse = ErrorResponse.builder()
                .field(field)
                .statusCode(statusCode.value())
                .build();

        return new ResponseEntity<>(errorResponse, statusCode);
    }

}
