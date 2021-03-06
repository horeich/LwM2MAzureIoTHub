// Copyright (c) Microsoft. All rights reserved.

package app.com.horeich.services.http;

import org.apache.http.Header;

public interface IHttpResponse {

    int getStatusCode();

    Header[] getHeaders();

    String getContent();

    boolean getIsRetriableError();

    default boolean isSuccessStatusCode() {
        return (getStatusCode() >= 200) && (getStatusCode() <= 299);
    }
}
