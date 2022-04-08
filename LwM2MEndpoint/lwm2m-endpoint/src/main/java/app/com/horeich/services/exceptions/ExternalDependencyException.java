// Copyright (c) Microsoft. All rights reserved.

package app.com.horeich.services.exceptions;

/**
 * Checked exception for invalid user input
 */
public class ExternalDependencyException extends BaseException {
    public ExternalDependencyException() {
    }

    public ExternalDependencyException(String message) {
        super(message);
    }

    public ExternalDependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
