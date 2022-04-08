package app.com.horeich.services.external;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Hashtable;

public class TwinApiModel {
    
    private String deviceId;
    // private Hashtable<String, String> telemetryMapping; // does not allow nulls as values

    // public TwinServiceModel(
    //     String deviceId) {
    //     this.deviceId = deviceId;
    // }

    @JsonProperty("DeviceId")
    public String getDeviceId() {
        return this.deviceId;
    }

    // public void setDeviceId(String deviceId) {
    //     this.deviceId = deviceId;
    // }

    // @JsonProperty("TelemetryMapping")
    // public Hashtable<String, String> getTelemetryMapping() {
    //     return this.telemetryMapping;
    // }

    // public void setTelemetryMapping(Hashtable<String, String> telemetryMapping) {
    //     this.telemetryMapping = telemetryMapping;
    // }
}
