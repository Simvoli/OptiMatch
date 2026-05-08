package com.optimatch.service;

// generic checked exception thrown by service layer
public class ServiceException extends Exception {

    // message only
    public ServiceException(String message) {
        super(message);
    }

    // message and underlying cause
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
