
// Copyright (c) Horeich UG

using System;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Horeich.Services.Models;

namespace Horeich.WebService.v1.Models
{
    public class TelemetryApiModel
    {
        private const String TIME_STAMP_IDENT = "Timestamp";

        /// Note: keys in URI must have the same name as dictionary name
        [JsonProperty(PropertyName = "Telemetry")]
        public Dictionary<String, LwM2MValue> Telemetry { get; set; }

        //private DateTime timeStamp = null;
        public DateTime Timestamp { get; set; }
        public TelemetryApiModel()
        {
            this.Telemetry = new Dictionary<string, LwM2MValue>();
        }
        public TelemetryServiceModel ToServiceModel()
        {
            var serviceModel = new TelemetryServiceModel();
            Dictionary<String, Object> serializeableTelmetry = new Dictionary<string, object>();
            foreach (KeyValuePair<string, LwM2MValue> lwM2mValue in Telemetry)
            {
                // TODO: error handling
                Type type = LwM2MValue.ConvertType(lwM2mValue.Value.Type); // double, int, bool, ...
                if (lwM2mValue.Value.Type.Equals("TIME"))
                {
                    long unixTime = (long)Convert.ChangeType(lwM2mValue.Value.Value, type);
                    DateTimeOffset dateTimeOffset = DateTimeOffset.FromUnixTimeMilliseconds(unixTime); // DateTimeOffset.FromUnixTimeSeconds(item.Value);
                    serviceModel.TimeStamp = dateTimeOffset.UtcDateTime;
                }
                else
                {
                    Object convertedValue = Convert.ChangeType(lwM2mValue.Value.Value, type);
                    serializeableTelmetry.Add(lwM2mValue.Key, convertedValue);
                    // serializeableTelmetry.Add(item.Key, obj);
                }
            }

            serviceModel.UTF8Message = JsonConvert.SerializeObject(serializeableTelmetry, Formatting.Indented);
            return serviceModel;
        }
    }

    
}