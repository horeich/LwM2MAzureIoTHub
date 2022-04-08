package app.com.horeich.services.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Hashtable;

public class ValueTypeListApiModel {

    private Hashtable<String,  Object[]> telemetry;

    @JsonProperty("Telemetry")
    public Hashtable<String,  Object[]> getTelemetry() {
        return this.telemetry;
    }

    public void setTelemetry(Hashtable<String, Object[]> telemetry) {
        this.telemetry = telemetry;
    }
}
