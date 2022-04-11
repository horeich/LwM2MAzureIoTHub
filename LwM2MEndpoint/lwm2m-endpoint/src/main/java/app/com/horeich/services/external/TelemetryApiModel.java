// Copyright (c) Microsoft. All rights reserved.

package app.com.horeich.services.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.leshan.core.model.ResourceModel;

public class TelemetryApiModel {

    private Map<String, LwM2MValue> telemetry;

    public TelemetryApiModel()
    {
        telemetry = new Hashtable<String, LwM2MValue>();
    }

    @JsonProperty("telemetry")
    public Map<String, LwM2MValue> getTelemetry() {
        return this.telemetry;
    }

    public void setTelemetry(Hashtable<String, LwM2MValue> telemetry) {
        this.telemetry = telemetry;
    }

    /**
     * Add resource to API model (includes translatiing resource value to JSON compatible exchange format)
     * @param variableName
     * @param resource
     */
    public void add(String variableName, LwM2mResource resource)
    {
        telemetry.put(variableName, LwM2MValue.Convert(resource));
        // TODO: throw if null?
    }


    // private String temperature;

    // private String humidity;

    // // Possible values - ["60000", "300000", "600000"] in milliseconds
    // private String timePeriod;

    // // private List<ConditionApiModel> conditions;

    // @JsonProperty("DateCreated")
    // public String getDateCreated() {
    //     return DateTime.now(DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ssZZ");
    // }

    // @JsonProperty("temperature")
    // public String getTemperature() {
    //     return temperature;
    // }

    // public void setTemperature(String temperature) {
    //     this.temperature = temperature;
    // }

    // @JsonProperty("humidity")
    // public String getHumidity() {
    //     return humidity;
    // }

    // public void setHumidity(String humidity) {
    //     this.humidity = humidity;
    // }
}