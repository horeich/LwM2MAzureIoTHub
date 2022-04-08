
package app.com.horeich.services.external;

import java.util.Hashtable;
import java.util.concurrent.CompletionStage;

import akka.stream.impl.Timers.Completion;
import app.com.horeich.services.exceptions.BaseException;

public interface IIoTHubBridgeClient {

    CompletionStage<Void> registerDeviceAsync(String serialNumber) throws BaseException;

    CompletionStage<Void> unregisterDeviceAsync(String endpointId) throws BaseException;

    CompletionStage<Void> sendTelemetryAsync(String endpointId, TelemetryApiModel content) throws BaseException;

    CompletionStage<Void> updatePropertiesAsync(String endpointId, PropertyApiModel content) throws BaseException;

    CompletionStage<PropertyApiModel> getPropertiesAsync(String endpointId) throws BaseException;

}