
// Copyright (c) Horeich UG

using System;
using System.Collections.Generic;
using Newtonsoft.Json;
using Horeich.Services.Models;
using Microsoft.Azure.Devices.Shared;
using Newtonsoft.Json.Linq;

namespace Horeich.IoTBridge.v1.Models
{
    public sealed class PropertyApiModel
    {
        /// Note: keys in URI must have the same name as dictionary name
        [JsonProperty(PropertyName = "Properties")]
        public Dictionary<String, LwM2MValue> Properties { get; set; }

        public PropertyApiModel()
        {
            this.Properties = new Dictionary<string, LwM2MValue>();
        }

        public PropertyApiModel(PropertyServiceModel model)
        {
            this.Properties = new Dictionary<string, LwM2MValue>();
            
            // Convert in dictionary
            foreach (KeyValuePair<string, object> property in model.Properties)
            {
                JToken value;
                var valueJson = JObject.Parse(property.Value.ToString());
                if (valueJson.TryGetValue("value", out value))
                {
                    // TODO: LWM2M Formatting class
                    LwM2MValue rawValue = new LwM2MValue(value.ToString(), "INTEGER");
                    this.Properties.Add(property.Key, rawValue);
                }
                // JToken value;
                // json.TryGetValue("value", out value);
                // this.Properties = model.Properties;
            }
        }

        public PropertyServiceModel ToServiceModel()
        {
            var serviceModel = new PropertyServiceModel();
            foreach (KeyValuePair<string, LwM2MValue> item in Properties)
            {
                // TODO: error handling
                Type type = LwM2MValue.ConvertType(item.Value.Type);
                object obj = Convert.ChangeType(item.Value.Value, type);
                serviceModel.Properties[item.Key] = obj;
            }
            return serviceModel;
        }
        // public List<string> Telemetry { get; set; }
        // [JsonProperty(PropertyName = "Telemetry")]
        // public String Telemetry{ get; set; }
    }
}