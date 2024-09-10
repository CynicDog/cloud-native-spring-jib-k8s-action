package com.example.catalogservice.web;

import com.example.catalogservice.domain.BookAlreadyExistsException;
import com.example.catalogservice.domain.BookNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class BookControllerAdvice {

    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String bookNotFoundHandler(BookNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(BookAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    String bookAlreadyExistsHandler(BookAlreadyExistsException ex) {
        return ex.getMessage();
    }

    // http POST http://localhost:9001/books author="Jon Snow" title="" isbn="123ABC456Z" price=9.90
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {

        var errors = new HashMap<String, String>();

        ex.getBindingResult().getAllErrors().forEach(err -> {
            String fieldName = ((FieldError) err).getField();
            String errMessage = err.getDefaultMessage();

            errors.put(fieldName, errMessage);
        });

        return errors;
    }
}
