package com.example.common.exception.controller;

import com.example.common.exception.ForbiddenException;
import com.example.docupload.model.ResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

    @ExceptionHandler(value = ForbiddenException.class)
    public ResponseEntity<Map<String,Object>> forbiddenException(ForbiddenException e){
        LOGGER.error("Forbidden Exception: "+ e.getMessage(), e.getCause());
        Map<String, Object> response = new HashMap<>();
        response.put("message","You're not authorized to perform this action");
        response.put("error","FORBIDDEN");
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = MaxUploadSizeExceededException.class)
    public ResponseObject maxUploadSizeExceededException(){
        return new ResponseObject("Sorry, we cannot process files larger than 5 MB.", false);
    }

}
