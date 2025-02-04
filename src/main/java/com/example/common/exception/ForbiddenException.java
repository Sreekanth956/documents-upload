package com.example.common.exception;

public class ForbiddenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ForbiddenException(){ super();}

    public ForbiddenException(String s){ super(s);}

}
