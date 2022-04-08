
// Copyright (c) Horeich UG

using System;
using System.Collections.Generic;
using Newtonsoft.Json;

namespace Horeich.Services.Models
{
    public class TelemetryServiceModel
    {
        /// Note: keys in URI must have the same name as dictionary name
        [JsonProperty(PropertyName = "Telemetry")]
        public String UTF8Message { get; set; }

        public DateTime TimeStamp { get; set; }

        public TelemetryServiceModel()
        {
            this.UTF8Message = String.Empty;
            DateTimeOffset dateTimeOffset = DateTimeOffset.FromUnixTimeSeconds(0);
            this.TimeStamp = dateTimeOffset.UtcDateTime;
        }
    }
}