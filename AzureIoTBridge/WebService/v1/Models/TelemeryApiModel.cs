
// Copyright (c) Horeich UG

using System;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;
using Horeich.Services.Models;

namespace Horeich.Services.v1.Models
{
    public class TelemetryApiModel
    {
        private const String TIME_STAMP_IDENT = "TimeStamp";

        /// Note: keys in URI must have the same name as dictionary name
        [JsonProperty(PropertyName = "Telemetry")]
        public Dictionary<String, LwM2MValue> Telemetry { get; set; }

        public DateTime TimeStamp { get; set; }

        public TelemetryServiceModel ToServiceModel()
        {
            var serviceModel = new TelemetryServiceModel();
            Dictionary<String, Object> serializeableTelmetry = new Dictionary<string, object>();
            foreach (KeyValuePair<string, LwM2MValue> item in Telemetry)
            {
                // TODO: error handling
                Type type = LwM2MValue.ConvertType(item.Value.Type);
                object obj = Convert.ChangeType(item.Value.Value, type);
                if (item.Key == TIME_STAMP_IDENT)
                {
                    DateTimeOffset dateTimeOffset = DateTimeOffset.FromUnixTimeSeconds((long)obj);
                    serviceModel.TimeStamp = dateTimeOffset.UtcDateTime;
                }
                else
                {
                    serializeableTelmetry.Add(item.Key, obj);
                }
            }

            serviceModel.UTF8Message = JsonConvert.SerializeObject(serializeableTelmetry, Formatting.Indented);
            return serviceModel;
        }
    }

    
}