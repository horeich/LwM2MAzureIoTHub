// Copyright (c) Microsoft. All rights reserved.

package app.com.horeich.services.exceptions;

/**
 * Checked exception for request errors.
 *
 * This exception is thrown when a client attempts to update a resource
 * providing the wrong Etag value. The client should retrieve the
 * resource again, to have the new Etag, and retry.
 */
public class ResourceOutOfDateException extends BaseException {
    public ResourceOutOfDateException() {
    }

    public ResourceOutOfDateException(String message) {
        super(message);
    }

    public ResourceOutOfDateException(String message, Throwable cause) {
        super(message, cause);
    }
}
