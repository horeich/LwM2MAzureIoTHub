// Copyright (c) Microsoft. All rights reserved.

package app.com.horeich.services.exceptions;

/**
 * Checked exception for request errors.
 *
 * This exception is thrown when a client attempts to create a resource
 * which would conflict with an existing one, for instance using the same
 * identifier. The client should change the identifier or assume the
 * resource has already been created.
 */
public class ConflictingResourceException extends BaseException {
    public ConflictingResourceException() {
    }

    public ConflictingResourceException(String message) {
        super(message);
    }

    public ConflictingResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
