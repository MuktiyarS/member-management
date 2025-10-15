package com.surest.member.exception;

import org.springframework.http.HttpStatus;

public class BusinessServiceException extends Exception {

    private HttpStatus httpStatus;

    public BusinessServiceException(String message) {
        super(message);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR; //default status
    }

    public BusinessServiceException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

