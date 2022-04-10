// Copyright (c) HOREICH UG. All rights reserved.

package app.com.horeich.services.external;

import java.util.Hashtable;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.eclipse.leshan.core.model.ResourceModel;

public class LwM2MValue {

    //@JsonProperty("value")
    private String value;

    //@JsonProperty("type")
    private String type;

    public LwM2MValue(String value, String type)
    {
        this.value = value;
        this.type = type;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    @JsonProperty("type")
    public String getType()
    {
        return this.type;
    }

    @JsonProperty("value")
    public String getValue() // TODO: throw
    {
        // TODO: error handling
        return this.value;
        // if (type.equals("INTEGER"))
        // {
        //     return Integer.parseInt(value);
        // }
        // else if (type.equals("LONG"))
        // {
        //     return Long.parseLong(value);
        // }
        // else if (type.equals("BOOL"))
        // {
        //     return Boolean.parseBoolean(value);
        // }
        // else if (type.equals("DOUBLE"))
        // {
        //     return Double.parseDouble(value);
        // }
        // else if (type.equals("STRING"))
        // {
        //     return value;
        // }
        // else if (type.equals("FLOAT"))
        // {
        //     return Float.parseFloat(value);
        // }
        // return null;
    }

    private ResourceModel.Type ConvertType()
    {
        // Float, double, string, integer, long, string

        if (type.equals("INTEGER"))
        {
            return ResourceModel.Type.INTEGER;
        }
        else if (type.equals("LONG"))
        {
            return ResourceModel.Type.INTEGER;
        }
        else if (type.equals("BOOL"))
        {
            return ResourceModel.Type.BOOLEAN;
        }
        else if (type.equals("DOUBLE"))
        {
            return ResourceModel.Type.FLOAT;
        }
        else if (type.equals("STRING"))
        {
            return ResourceModel.Type.STRING;
        }
        else if (type.equals("FLOAT"))
        {
            return ResourceModel.Type.FLOAT;
        }
        else
        {
            return ResourceModel.Type.NONE;
            // TODO: unsupportedtypeexception
            // Unknown payload type
            //throw new InvalidCastException("data type not found");
        }
    }
}
