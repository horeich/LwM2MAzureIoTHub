package app.com.horeich.services.external;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import app.com.horeich.services.http.*;
import app.com.horeich.services.exceptions.ExternalDependencyException;
import app.com.horeich.services.helpers.IHttpClientWrapper;
import app.com.horeich.services.runtime.IServicesConfig;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.concurrent.CompletionStage;
import app.com.horeich.services.exceptions.*;

import app.com.horeich.services.helpers.*;

import play.Logger;

@Singleton
public class TelemetryAdapter {
    
    private static final Logger.ALogger log = Logger.of(TelemetryAdapter.class);

    private HttpClientWrapper client;
    private String serviceUri;

    @Inject
    public TelemetryAdapter(
        HttpClientWrapper client,
        IServicesConfig config)
    {
        this.client = client;
        this.serviceUri = config.getTelemetryUrl();
    }

    // In future put LWM2M object here
    public CompletionStage UpdateTelemetryAsync(TelemetryApiModel telemetry) throws ExternalDependencyException {
        //HttpRequest request = CreateRequest("testpath");
        try {
            return this.client.putAsync("asdf", "asdf", null);
                //String.format("LEV2/%s/?%s&%s", "telemetry", telemetry.getTemperature(), telemetry.getHumidity(),"a"));
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            throw new ExternalDependencyException("UpdateRule failed");
        }
    }

    // private HttpRequest CreateRequest(String path, TelemetryApiModel content) throws InvalidConfigurationException {
    //     try {
    //         HttpRequest request = new HttpRequest();
    //         request.setUriFromString(String.format("LEV2/%s/?%s&%s", "telemetry", content.getTemperature(), content.getHumidity()));
    //         request.getOptions().setAllowInsecureSSLServer(true);
    //         return request;
    //     } catch (URISyntaxException e) { // TODO: UnsupportedEncodingException if json
    //         throw new InvalidConfigurationException("Unable to create http request", e);
    //     }
    // }

    private HttpRequest CreateRequest(String path) throws InvalidConfigurationException {
        //return CreateRequest(path, null);
        return null;
    }
}