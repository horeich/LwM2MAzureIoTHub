
using System;
using Newtonsoft.Json;

public class LwM2MValue
{
    [JsonProperty(PropertyName = "Value")]
    public String Value { get; set; }

    [JsonProperty(PropertyName = "Type")]
    public String Type { get; set; }

    public LwM2MValue(String value, String type)
    {
        this.Value = value;
        this.Type = type;
    }

    public static Type ConvertType(String dataType)
    {
        if (String.Compare(dataType, "INTEGER") == 0)
        {
            return typeof(int);
        }
        else if (String.Compare(dataType, "LONG") == 0)
        {
            return typeof(long);
        }
        else if (String.Compare(dataType, "BOOL") == 0)
        {
            return typeof(bool);
        }
        else if (String.Compare(dataType, "DOUBLE") == 0)
        {
            return typeof(double);
        }
        else if (String.Compare(dataType, "STRING") == 0)
        {
            return typeof(string);
        }
        else if (String.Compare(dataType, "FLOAT") == 0)
        {
            return typeof(float);
        }
        else
        {
            // Unknown payload type
            throw new InvalidCastException("data type not found");
        }
    }
}