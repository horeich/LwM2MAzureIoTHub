// Copyright (c) Microsoft. All rights reserved.

package app.com.horeich.services.exceptions;

/**
 * Checked exception for invalid user input
 */
public class SeedException extends BaseException {
    public SeedException() {
    }

    public SeedException(String message) {
        super(message);
    }

    public SeedException(String message, Throwable cause) {
        super(message, cause);
    }
}
