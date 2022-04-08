package app.com.horeich.services.external;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import com.google.j2objc.annotations.Property;

import org.apache.http.HttpStatus;
import org.eclipse.californium.core.observe.Observation;

import app.com.horeich.services.exceptions.BaseException;
import app.com.horeich.services.exceptions.ConflictingResourceException;
import app.com.horeich.services.exceptions.ExternalDependencyException;
import app.com.horeich.services.exceptions.InvalidConfigurationException;
import app.com.horeich.services.exceptions.ResourceNotFoundException;
import app.com.horeich.services.http.HttpRequest;
import app.com.horeich.services.http.IHttpClient;
import app.com.horeich.services.http.IHttpRequest;
import app.com.horeich.services.http.IHttpResponse;
import app.com.horeich.services.runtime.IServicesConfig;
import play.Logger;
import play.libs.Json;

public class AzureBridgeClient implements IIoTHubBridgeClient {

    private static final Logger.ALogger log = Logger.of(RegistrationAdapter.class);

    private final IHttpClient httpClient;
    private final String telemetryUri;

    public AzureBridgeClient(IHttpClient httpClient, IServicesConfig config) {
        this.httpClient = httpClient;
        this.telemetryUri = config.getTelemetryUrl();
    }

    private static <A> A fromJson(String json, Class<A> clazz) {
        return Json.fromJson(Json.parse(json), clazz);
    }

    @Override
    public CompletionStage<Void> registerDeviceAsync(String endpointId) throws BaseException {
        // TwinApiModel model = new TwinApiModel();
        HttpRequest request = CreateRequest(String.format("v1/%s/register", endpointId));
        // TODO: https://stackoverflow.com/questions/797834/should-a-restful-put-operation-return-something
        return httpClient.postAsync(request).thenAcceptAsync(m -> {
            try {
                CheckStatusCode(m, request); // throws
                return;
                // return fromJson(m.getContent(), TwinApiModel.class);
            } catch (Exception e) {
                throw new CompletionException("Unable to create resource " + request.getUri(), e);
            }
        });
    }

    @Override
    public CompletionStage<Void> unregisterDeviceAsync(String endpointId) throws BaseException {
        // TwinApiModel model = new TwinApiModel();
        HttpRequest request = CreateRequest(String.format("v1/%s/unregister", endpointId));

        return httpClient.postAsync(request).thenAcceptAsync(m -> {
            try {
                CheckStatusCode(m, request); // throws
                return;
                // return fromJson(m.getContent(), TwinApiModel.class);
            } catch (Exception e) {
                throw new CompletionException("Unable to create resource " + request.getUri(), e);
            }
        });
    }


    @Override
    public CompletionStage<Void> sendTelemetryAsync(String endpointId, TelemetryApiModel content) throws BaseException {
        HttpRequest request = CreateRequest(String.format("v1/%s/telemetry", endpointId), content);
        return httpClient.putAsync(request).thenAcceptAsync(response -> {
            try {
                CheckStatusCode(response, request); // throws exception
                return;
            } catch (Exception e) {
                throw new CompletionException("Unable to update resource " + request.getUri(), e);
            }
        });
    }

    @Override
    public CompletionStage<PropertyApiModel> getPropertiesAsync(String endpointId) throws BaseException {
        try {
            HttpRequest request = CreateRequest(String.format("v1/%s/properties", endpointId));
            //HttpRequest request = new HttpRequest();
            //request.setUriFromString(telemetryUri + "/" + "v1/%s/properties"); // throws URISyntaxException
            // request.getOptions().setAllowInsecureSSLServer(true);

            return httpClient.getAsync(request).thenApplyAsync(response -> {
                try {
                    CheckStatusCode(response, request);
                    return fromJson(response.getContent(), PropertyApiModel.class);
                } catch (Exception e) {
                    throw new CompletionException("Unable to retrieve" + request.getUri(), e);
                }
            });
        } catch (Exception e) {
            throw new InvalidConfigurationException("Unable to create http request", e);
        }
    }

    @Override
    public CompletionStage<Void> updatePropertiesAsync(String endpointId, PropertyApiModel content) throws BaseException {
        HttpRequest request = CreateRequest(String.format("v1/%s/properties", endpointId), content);
        return httpClient.putAsync(request).thenAcceptAsync(response -> {
            try {
                CheckStatusCode(response, request); // throws exception
                return;
            } catch (Exception e) {
                throw new CompletionException("Unable to update resource " + request.getUri(), e);
            }
        });
    }

    private HttpRequest CreateRequest(String path) throws InvalidConfigurationException {
        try {
            HttpRequest request = new HttpRequest();
            request.setUriFromString(telemetryUri + "/" + path); // throws URISyntaxException
            return request;
        } catch (URISyntaxException e) {
            throw new InvalidConfigurationException("Unable to create http request", e);
        }
    }


    private HttpRequest CreateRequest(String path, TelemetryApiModel content) throws InvalidConfigurationException {
        try {
            HttpRequest request = new HttpRequest();
            request.setUriFromString(telemetryUri + "/" + path); // throws URISyntaxException
            request.getOptions().setAllowInsecureSSLServer(true);
            if (content != null) {

                // HTTPClient must support the encoding
                request.setContent(content); // throws UnsupportedEncodingException
            }
            return request;
        } catch (UnsupportedEncodingException | URISyntaxException e) {
            throw new InvalidConfigurationException("Unable to create http request", e);
        }
    }

    private HttpRequest CreateRequest(String path, PropertyApiModel content) throws InvalidConfigurationException {
        try {
            HttpRequest request = new HttpRequest();
            request.setUriFromString(telemetryUri + "/" + path); // throws URISyntaxException
            request.getOptions().setAllowInsecureSSLServer(true);
            if (content != null) {

                // HTTPClient must support the encoding
                request.setContent(content); // throws UnsupportedEncodingException
            }
            return request;
        } catch (UnsupportedEncodingException | URISyntaxException e) {
            throw new InvalidConfigurationException("Unable to create http request", e);
        }
    }


    // 
    private void CheckStatusCode(IHttpResponse response, IHttpRequest request) throws BaseException {
        if (response.isSuccessStatusCode()) {
            return;
        }
        log.info(String.format("StorageAdapter returns %s for request %s", response.getStatusCode(),
                request.getUri().toString()));
        switch (response.getStatusCode()) {
            case HttpStatus.SC_NOT_FOUND:
                throw new ResourceNotFoundException(
                        response.getContent() + ", request URL = " + request.getUri().toString());

            case HttpStatus.SC_CONFLICT:
                throw new ConflictingResourceException(
                        response.getContent() + ", request URL = " + request.getUri().toString());

            default:
                throw new ExternalDependencyException(
                        String.format("Http request failed, status code = %s, content = %s, request URL = %s",
                                response.getStatusCode(), response.getContent(), request.getUri().toString()));
        }
    }
}