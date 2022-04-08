// Copyright (c) Microsoft. All rights reserved.

package app.com.horeich.services.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Hashtable;

import org.eclipse.leshan.core.model.ResourceModel;

public class TelemetryApiModel {

    @JsonProperty("telemetry")
    private Hashtable<String, LwM2MValue> telemetry;

    public TelemetryApiModel()
    {
        telemetry = new Hashtable<String, LwM2MValue>();
    }

    public Hashtable<String, LwM2MValue> getTelemetry() {
        return this.telemetry;
    }

    public void setValue(String variableName, LwM2mResource resource)
    {
        Object rawValue  = resource.getValue();
        
        if (resource.getType() == ResourceModel.Type.BOOLEAN)
        {
            String valueStr = Boolean.toString((Boolean)rawValue);
            telemetry.put(variableName, new LwM2MValue(valueStr, "BOOL"));
        }
        else if (resource.getType() == ResourceModel.Type.FLOAT)
        {
            String valueStr = Double.toString((Double)rawValue);
            telemetry.put(variableName, new LwM2MValue(valueStr, "FLOAT"));
        }
        else if (resource.getType() == ResourceModel.Type.INTEGER)
        {
            String valueStr = Integer.toString((Integer)rawValue);
            telemetry.put(variableName, new LwM2MValue(valueStr, "INTEGER"));
        }
    
        else if (resource.getType() == ResourceModel.Type.TIME)
        {
            // TODO:
        }
        else
        {
            //throw;
        }
       
    }


    private String temperature;

    private String humidity;

    // Possible values - ["60000", "300000", "600000"] in milliseconds
    private String timePeriod;

    // private List<ConditionApiModel> conditions;

    @JsonProperty("DateCreated")
    public String getDateCreated() {
        return DateTime.now(DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ssZZ");
    }

    @JsonProperty("temperature")
    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    @JsonProperty("humidity")
    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }
}