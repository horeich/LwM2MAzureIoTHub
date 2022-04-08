package app.com.horeich.services.external;

import java.util.concurrent.*;

import org.eclipse.leshan.server.californium.LeshanServer;

import app.com.horeich.services.http.*;
import app.com.horeich.services.runtime.IServicesConfig;
import app.com.horeich.services.exceptions.*;
import play.Logger;
import play.libs.Json;

public class RegistrationAdapter implements IRegistrationAdapter {

    private static final Logger.ALogger log = Logger.of(RegistrationAdapter.class);
    
    private final IHttpClient httpClient;
    private final String registrationUri;

    public RegistrationAdapter(IHttpClient httpClient, IServicesConfig config) {
        this.httpClient = httpClient;
        this.registrationUri= config.getRegistrationUrl();
    }

    private static <A> A fromJson(String json, Class<A> clazz) {
        return Json.fromJson(Json.parse(json), clazz);
    }

    @Override
    public CompletionStage<Void> RegisterDeviceAsync(String endpointId) throws BaseException {
        // TwinApiModel model = new TwinApiModel();
        HttpRequest request = CreateRequest(String.format("v1/%s", endpointId));

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

    private HttpRequest CreateRequest(String path, TwinApiModel content) throws InvalidConfigurationException {
        try {
            HttpRequest request = new HttpRequest();
            request.setUriFromString(registrationUri + "/" + path);
            request.getOptions().setAllowInsecureSSLServer(true);
            if (content != null) {
                request.setContent(content);
            }
            return request;
        } catch (Exception e) { // UnsupportedEncodingException | URISyntaxException
            throw new InvalidConfigurationException("Unable to create http request", e);
        }
    }

    private HttpRequest CreateRequest(String path) throws InvalidConfigurationException {
        return CreateRequest(path, null);
    }

    private void CheckStatusCode(IHttpResponse response, IHttpRequest request) { // throws BaseException {
        if (response.isSuccessStatusCode()) {
            return;
        }
        log.info(String.format("StorageAdapter returns %s for request %s", response.getStatusCode(),
                request.getUri().toString()));

        // switch (response.getStatusCode()) {
        // case HttpStatus.SC_NOT_FOUND:
        // throw new ResourceNotFoundException(
        // response.getContent() + ", request URL = " + request.getUri().toString());

        // case HttpStatus.SC_CONFLICT:
        // throw new ConflictingResourceException(
        // response.getContent() + ", request URL = " + request.getUri().toString());

        // default:
        // throw new ExternalDependencyException(
        // String.format("Http request failed, status code = %s, content = %s, request
        // URL = %s", response.getStatusCode(), response.getContent(),
        // request.getUri().toString()));
        // }
    }

}