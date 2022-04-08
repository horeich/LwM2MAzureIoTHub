// Copyright (c) HOREICH UG. All rights reserved.

package app.com.horeich.services.external;

import java.util.Hashtable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.eclipse.leshan.core.node.LwM2mResource;

public class PropertyApiModel {

    @JsonProperty("properties")
    private Map<String, LwM2MValue> properties;

    public PropertyApiModel()
    {
        properties = new Hashtable<String, LwM2MValue>();
    }

    public Map<String, LwM2MValue> getProperties() {
        return this.properties;
    }

    public void setProperties(Hashtable<String, LwM2MValue> properties) {
        this.properties = properties;
    }

    public void setValue(String variableName, LwM2mResource resource)
    {

    }

    public LwM2MValue getValue(String resource)
    {
        return this.properties.get(resource);
    }

}
