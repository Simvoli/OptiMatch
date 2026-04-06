package com.optimatch.service;

/**
 * Exception thrown by service layer operations.
 * Wraps underlying exceptions (SQL, validation, etc.) with user-friendly messages.
 */
public class ServiceException extends Exception {

    /**
     * Creates a ServiceException with the specified message.
     *
     * @param message the error message
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * Creates a ServiceException with the specified message and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
