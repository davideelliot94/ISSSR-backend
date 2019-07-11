package com.isssr.ticketing_system.exception;

public class TokenExpiredException extends Exception{

    public TokenExpiredException() {
        super();
    }

    public TokenExpiredException(String message) {
        super(message);
    }


}
