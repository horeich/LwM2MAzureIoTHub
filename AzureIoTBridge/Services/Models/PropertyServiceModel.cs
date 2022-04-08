
// Copyright (c) Horeich UG

using System;
using System.Collections.Generic;
using Newtonsoft.Json;
using Microsoft.Azure.Devices.Shared;

namespace Horeich.Services.Models
{
    public class PropertyServiceModel
    {
        /// Note: keys in URI must have the same name as dictionary name
        [JsonProperty(PropertyName = "Properties")]
        public TwinCollection Properties { get; set; }

        public PropertyServiceModel()
        {
            this.Properties = new TwinCollection();
        }

        //public List<string> Telemetry { get; set; }
        // [JsonProperty(PropertyName = "Telemetry")]
        // public String Telemetry{ get; set; }
    }
}