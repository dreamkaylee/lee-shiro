package com.i5018.shiro.exception;

import org.apache.shiro.authc.AccountException;

/**
 * @author limk
 * @date 2020/8/25 16:16
 */
public class ExpiredAccountException extends AccountException {

    /**
     * Creates a new ExpiredAccountException.
     */
    public ExpiredAccountException() {
        super();
    }

    /**
     * Constructs a new ExpiredAccountException.
     *
     * @param message the reason for the exception
     */
    public ExpiredAccountException(String message) {
        super(message);
    }

    /**
     * Constructs a new ExpiredAccountException.
     *
     * @param cause the underlying Throwable that caused this exception to be thrown.
     */
    public ExpiredAccountException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new ExpiredAccountException.
     *
     * @param message the reason for the exception
     * @param cause   the underlying Throwable that caused this exception to be thrown.
     */
    public ExpiredAccountException(String message, Throwable cause) {
        super(message, cause);
    }

}
