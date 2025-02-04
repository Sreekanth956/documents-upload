package com.example.common.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private HttpStatus status;

    public ApiException(){ super();}

    public ApiException(String message, Throwable cause){ super(message,cause);}

    public ApiException(String message){ super(message);}

    public ApiException(Throwable cause){ super(cause);}

    public ApiException(HttpStatus status, String message){
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus(){ return status;}

    public void setStatus(HttpStatus status){ this.status = status; }

}
