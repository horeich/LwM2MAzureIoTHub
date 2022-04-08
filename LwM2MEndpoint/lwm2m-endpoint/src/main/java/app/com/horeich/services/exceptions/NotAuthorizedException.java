// Copyright (c) Microsoft. All rights reserved.

package app.com.horeich.services.exceptions;

/**
 * Exception for authorization errors.
 *
 * This exception is thrown when the user or the application
 * is not authorized to perform an action.
 */
public class NotAuthorizedException extends BaseException {
    public NotAuthorizedException() {
    }

    public NotAuthorizedException(String message) {
        super(message);
    }

    public NotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
