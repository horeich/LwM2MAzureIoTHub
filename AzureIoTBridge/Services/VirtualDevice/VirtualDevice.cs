using System;
using System.Linq;
using System.Threading.Tasks;
using System.Collections.Generic;
using System.Collections.Concurrent;

using System.Text;
using Newtonsoft.Json;
using System.Threading;
using Microsoft.Azure.Devices.Client;
using Microsoft.Azure.Devices.Shared;
// using Microsoft.Azure.Devices;

using Horeich.SensingSolutions.Services.Runtime;
using Horeich.SensingSolutions.Services.Exceptions;
using Horeich.SensingSolutions.Services.Http;
using Horeich.SensingSolutions.Services.Diagnostics;

using Horeich.Services.Models;

using Newtonsoft.Json.Linq;
using Newtonsoft.Json.Serialization;
using Newtonsoft.Json.Schema;
using Newtonsoft.Json.Converters;


using Microsoft.Azure.DigitalTwins.Parser;

namespace Horeich.SensingSolutions.Services.VirtualDevice
{
    public class VirtualDevice : IVirtualDevice, IDisposable
    {
        private ILogger _log;
        private DeviceClient _deviceClient;
        //private RegistryManager _registryManager;

        // private TelemetryMapping _telemetryMapping;
        private List<Tuple<string, Type>> _mapping = new List<Tuple<string, Type>>();
        private readonly string _deviceId;
        private readonly string _deviceKey;
        private readonly string _hubString;
        private Dictionary<string, string> _properties;
        private readonly TimeSpan _sendInterval;
        public DeviceUptime _uptime;
        private bool _disposed = false;
        private const int propertyUpdateTime = 10000;
        private bool _connected;

        private const string SENT_TIME_UTC = "iothub-creation-time-utc";


        private Twin _twin;

        private ConcurrentDictionary<String, Object> _changedProperties = new ConcurrentDictionary<String, Object>();

        private TwinServiceModel _twinModel;

        private void OnConnectionStatusChange(ConnectionStatus status, ConnectionStatusChangeReason reason)
        {
            _log.Info(string.Format("Connection status changed to {0}, because {1}", status, reason), () => {});
        }
        
        private async Task OnDesiredPropertyChanged(TwinCollection desiredProperties, object userContext)
        {
            // TODO: Error handling
            foreach (KeyValuePair<string, object> desiredProperty in desiredProperties)
            {
                // Adds value to properties or updates existing value
                // see https://docs.microsoft.com/en-us/dotnet/api/system.collections.concurrent.concurrentdictionary-2.addorupdate?redirectedfrom=MSDN&view=net-6.0#overloads
                _changedProperties.AddOrUpdate(desiredProperty.Key, desiredProperty.Value, (oldkey, oldvalue) => desiredProperty.Value);
            }
        }

        public TwinCollection FetchChangedProperties()
        {
            TwinCollection changedProperties = new TwinCollection();
            foreach (KeyValuePair<string, object> changedProperty in _changedProperties)
            {
                object propertyValue;
                if (_changedProperties.TryRemove(changedProperty.Key, out propertyValue)) // remove property from pending downlink list
                {
                    changedProperties[changedProperty.Key] = propertyValue;
                }
            }
            return changedProperties;
        }

        public async Task UpdateReportedProperties(TwinCollection reportedProperties)
        {
            Twin deviceTwin = await _deviceClient.GetTwinAsync();
            TwinCollection desiredProperties = deviceTwin.Properties.Desired;

            // TODO: Error handling
            foreach (KeyValuePair<String, Object> reportedProperty in reportedProperties)
            {   
                TwinCollection acknowledgedProperty = new TwinCollection();

                /// There is just ONE version value for ALL properties (the biggest value)
                /// If multiple desired properties are updated, all poperties will have the reported property version +1 
                   
                // JObject desiredProperty = desiredProperties[reportedProperty.Key];
                acknowledgedProperty["value"] = reportedProperty.Value;
                acknowledgedProperty["av"] = desiredProperties.Version; // automatically increases reported version to desired version +1
                acknowledgedProperty["ac"] = 200; // OK
                reportedProperties[reportedProperty.Key] = acknowledgedProperty;
            }
            await _deviceClient.UpdateReportedPropertiesAsync(reportedProperties);
        }

        public async Task SendMessageAsync(string utf8Message, DateTime timeStamp)
        {
            // Sends a json encoded message
            // TODO: error handling

            CancellationTokenSource cts = new CancellationTokenSource();
            cts.CancelAfter(4000);

            var eventMessage = default(Message); // TODO: using and error handling
            eventMessage = new Message(Encoding.UTF8.GetBytes(utf8Message));

            eventMessage.ContentType = "application/json";
            eventMessage.ContentEncoding = "utf-8";

            if (((DateTimeOffset)timeStamp).ToUnixTimeSeconds() != 0)
            {
                eventMessage.Properties.Add(SENT_TIME_UTC, timeStamp.ToString()); // format: "2021-01-29T16:45:39.021Z"
            }

            _log.Info(string.Format("{0} > Sending telemetry: {1}", timeStamp, eventMessage), () => {});

            await _deviceClient.SendEventAsync(eventMessage);


            // Create message and forward it to iot hub
            //using (var message = new Microsoft.Azure.Devices.Client.Message(Encoding.ASCII.GetBytes(payload))) // TODO: UTF8?
            //{
               // ContentEncoding 

                // Send event but cancel after given timeout (OperationCanceledException)
                // message.Properties.Add("iothub-creation-time-utc", timeStamp.ToString());
                // message.Properties.Add("SendInterval", "44");

                
                // Task sendTask = _deviceClient.SendEventAsync(message, cts.Token);
                //_log.Info(string.Format("{0} > Sending telemetry: {1}", DateTime.Now, messageString), () => {});
                // await sendTask;
            //}
        }

        public VirtualDevice(DeviceCredentials credentials, ILogger logger)
        {
            _log = logger;            
        }

        public VirtualDevice(DeviceClient deviceClient, ILogger logger)
        {
            _deviceClient = deviceClient;
            _log = logger;

            _deviceClient.SetConnectionStatusChangesHandler(OnConnectionStatusChange); // does not throw
        }

        public async Task ConnectAsync()
        {
            // TODO: error handling

            await _deviceClient.OpenAsync();

            await _deviceClient.SetDesiredPropertyUpdateCallbackAsync(OnDesiredPropertyChanged, null); // triggers OpenAsync()

            await RetrieveDeviceTwin();

            //TwinCollection reportedProperties = new TwinCollection();
            //reportedProperties["Status"] = "online";
            //await _deviceClient.UpdateReportedPropertiesAsync(reportedProperties); 
        }

        public async Task DisconnectAsync()
        {
            // This function is only allowed to be called from a single context
            if (_deviceClient != null)
            {
                // TODO: throws OperationCanceledException
                await _deviceClient.CloseAsync();
            }

            Dispose(); // make sure created resources are disposed
        }

        public VirtualDevice(TwinServiceModel model, ILogger logger)
        {
            _log = logger;
            _twinModel = model;
        }

        private VirtualDevice(DeviceApiModel model, ILogger logger)
        {
            _log = logger;
            _mapping = model.Mapping;
            _deviceId = "LEV2";
            _deviceKey = "bWyXou7mwmlzyWj1Ftr/+QOzfZQt+utorCwBLwqEob8=";
            _hubString = "HHub.azure-devices.net";//;SharedAccessKeyName=iothubowner;SharedAccessKey=JM6aRNjmVDQUUQSzF/K1sngKqa39Rm5ZTTaHQ7QMcyg=";
            _sendInterval = TimeSpan.FromSeconds(model.SendInterval);
            _properties = model.Properties;
            _properties.Add("Status", "online");

            _uptime = new DeviceUptime();

            // Create iot hub device client
            Microsoft.Azure.Devices.Client.IAuthenticationMethod authMethod = 
                new DeviceAuthenticationWithRegistrySymmetricKey(_deviceId, _deviceKey);
            _deviceClient = DeviceClient.Create(_hubString, authMethod, Microsoft.Azure.Devices.Client.TransportType.Amqp);
            // _deviceClient.OpenAsync()
        }

        ~VirtualDevice()
        {
            Dispose(false);
        }

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected virtual void Dispose(bool disposing)
        {
            _log.Debug("Client has been disposed", () => { });
            if (!_disposed)
            {
                if (disposing)
                {
                    _deviceClient.Dispose();
                }
                _disposed = true;
            }
        }

        public async Task<bool> IsActive()
        {
            TimeSpan test = _uptime.Duration;
            if (TimeSpan.Compare(test, _sendInterval) > 0)
            {
                _properties["Status"] = "offline";
                await UpdateDevicePropertiesAsync(_properties, propertyUpdateTime);
                return false;
            }
            return true;
        }

        public async Task RetrieveDeviceTwin()
        {
            Twin deviceTwin = await _deviceClient.GetTwinAsync();
            
            TwinCollection desiredProperties = deviceTwin.Properties.Desired;
            TwinCollection reportedProps = deviceTwin.Properties.Reported;
            await OnDesiredPropertyChanged(desiredProperties, null);
        }

        // public async Task SyncDeviceFunctionsAsync(TelemetryApiModel model)
        // {
        //     // Find out wheter telemetry is property or telemetry
        //     // TODO: rename telemetry in paypload
            
        //     Twin deviceTwin = await _deviceClient.GetTwinAsync();
            
        //     TwinCollection desiredProperties = deviceTwin.Properties.Desired; // User-changeable properties

        //     // TODO: what happens if property does not exist??

        //     TwinCollection updatedProperties = new TwinCollection();

        //     // Handle properties
        //     foreach (KeyValuePair<String, List<Object>> payloadValue in model.Telemetry)
        //     {
        //         if (desiredProperties.Contains(payloadValue.Key))
        //         {
        //             Type propertyType = desiredProperties.GetType(); 
                    
        //             // TODO: type matching?
        //             Type type = TypeFromString(payloadValue.Value[1].ToString());
        //             updatedProperties[payloadValue.Key] = Convert.ChangeType(payloadValue.Value[0], propertyType);
        //         }
        //     }

        //     foreach (KeyValuePair<String, Object> updatedProperty in updatedProperties)
        //     {
        //         if (model.Telemetry.ContainsKey(updatedProperty.Key))
        //         {
        //             model.Telemetry.Remove(updatedProperty.Key);
        //         }
        //     }

        //     await SyncDevicePropertiesAsync(updatedProperties);
            
        //     // Handle telemetry

        //     DateTime timeStamp = DateTime.UtcNow;

        //     Dictionary<string, object> serializeableData = new Dictionary<string, object>();
        //     foreach (KeyValuePair<String, List<Object>> value in model.Telemetry)
        //     {
        //         // TODO: TypeFromString
        //         if (value.Key == "Timestamp")
        //         {
        //             Type t = TypeFromString(value.Value[1].ToString());
        //             long unixTimeStamp = (long)Convert.ChangeType(value.Value[0], t);
        //             DateTimeOffset dateTimeOffset = DateTimeOffset.FromUnixTimeSeconds(unixTimeStamp);
        //             timeStamp = dateTimeOffset.UtcDateTime;
        //         }

        //         Type type = TypeFromString(value.Value[1].ToString());
        //         serializeableData.Add(value.Key, Convert.ChangeType(value.Value[0], type));
        //     }

        //     var telemetry = JsonConvert.SerializeObject(serializeableData, Formatting.Indented);
        //     await SendMessageAsync(telemetry, timeStamp);
        // }

        private async Task SyncDevicePropertiesAsync(TwinCollection properties)
        {
            CancellationTokenSource cts = new CancellationTokenSource();
            cts.CancelAfter(4000);
            await _deviceClient.UpdateReportedPropertiesAsync(properties, cts.Token);
        }

        

        private Type TypeFromString(string dataType)
        {
            if (String.Compare(dataType, "INTEGER") == 0)
            {
                return typeof(int);
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
                throw new DevicePayloadTypeException("data type not found");
            }
        }

        

        // public async Task SendDeviceTelemetryAsync(List<string> telemetryDataPoints, int timeout)
        // {
        //     // Assign value to variable names
        //     Dictionary<string, object> serializeableData = new Dictionary<string, object>();

        //     // Thingsboard requires extra device id string
        //     serializeableData.Add("deviceId", _deviceId);
        //     serializeableData.Add("temp", 5);
        //     // for (int i = 0; i < telemetryDataPoints.Count; ++i)
        //     // {
        //     //     Type type = _mapping[i].Item2;

        //          //serializeableData.Add(_mapping[i].Item1, Convert.ChangeType(telemetryDataPoints[i], type));
        //     // }

        //     var payload = JsonConvert.SerializeObject(serializeableData, Formatting.Indented);

        //     // await SendTelemetryAsync(payload, timeout);
        // }

        // public async Task SendDeviceTelemetryAsync(string telemetryDataPoints, int timeout)
        // {
        //     // foreach (var item in _twinModel.TelemetryMapping)
        //     // {
        //     //     // TODO: ArgumentOutOfRangeException
        //     //     String hexStringValue = telemetryDataPoints.Substring(i*8, 8);
        //     //     UInt32 byteValue = uint.Parse(hexStringValue, System.Globalization.NumberStyles.AllowHexSpecifier);
        //     //     Byte[] binValue = BitConverter.GetBytes(byteValue);

        //     //     switch (item.Value)
        //     //     {
        //     //         case DTEntityKind.Float:
        //     //             float value = BitConverter.ToSingle(binValue);
        //     //             _logger.Debug(String.Format("Decimal value {0}", value), () => {});
        //     //             break;
        //     //         case DTEntityKind.Integer:
        //     //             Int32 value1 = BitConverter.ToInt32(binValue);
        //     //             _logger.Debug(String.Format("Decimal value {0}", value1), () => {});
        //     //             break;
        //     //     }
        //     // }
        // }

        private async Task UpdateDevicePropertiesAsync(Dictionary<string, string> propertyDataPoints, int timeout)
        {
            TwinCollection reportedProperties = new TwinCollection();
            foreach (KeyValuePair<string, string> property in propertyDataPoints)
            {
                reportedProperties[property.Key] = property.Value;
            }

            CancellationTokenSource cts = new CancellationTokenSource();
            cts.CancelAfter(timeout);
            await _deviceClient.UpdateReportedPropertiesAsync(reportedProperties, cts.Token);

            // Twin twin = await _deviceClient.
        }

    }
}




            // if not in desired collection update properties blindly (we cannot know about components!)


            // dismiss all unknown properties




            //String deviceId = "LEV1";
            // Twin twin = await _registryManager.GetTwinAsync(deviceId);

            // bool propertyFound = false;
            // bool updatedDownstream = false;
            // foreach (KeyValuePair<String, Object> reportedProperty in reportedProperties)
            // {
            //     foreach (KeyValuePair<String, Object> downlinkProperty in _downlinkProperties)
            //     {
            //         if (downlinkProperty.Key == reportedProperty.Key)
            //         {
            //             updatedDownstream = downlinkProperty.Value != reportedProperty.Value;
            //             propertyFound = true;
            //             break;
            //         }
            //     }
            // TwinCollection ackProps = new TwinCollection();
            // ackProps["value"] = 33;
            // ackProps["ac"] = 200;
            // ackProps["av"] = 12; 
            // ackProps["ad"] = "desired property received";
            // reportedProperties["SendInterval"] = ackProps;
            //     // if (!propertyFound || updatedDownstream)
            //     // {
            //     //     var patch = String.Format(
            //     //     @"{
            //     //         ""properties"": {
            //     //         ""desired"": {
            //     //             ""{0}"": ""{1}""
            //     //         }
            //     //         }
            //     //     }", reportedProperty.Key, reportedProperty.Value);

            //     //     await _registryManager.UpdateTwinAsync(_deviceId, patch, _twin.ETag);
            //     // }
            //     // }HostName=HHub.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=JM6aRNjmVDQUUQSzF/K1sngKqa39Rm5ZTTaHQ7QMcyg=
            // }

                        // CancellationTokenSource cts = new CancellationTokenSource();
            // cts.CancelAfter(4000);

            //Dictionary<string, object> serializeableData = new Dictionary<string, object>();

            // // Thingsboard requires extra device id string
            //serializeableData.Add("deviceId", _deviceId);
            //serializeableData.Add("temp", 5);
            // // for (int i = 0; i < telemetryDataPoints.Count; ++i)
            // // {
            // //     Type type = _mapping[i].Item2;

            //      //serializeableData.Add(_mapping[i].Item1, Convert.ChangeType(telemetryDataPoints[i], type));
            // // }

            //var payload = JsonConvert.SerializeObject(serializeableData, Formatting.Indented);

            // using (var message = new Microsoft.Azure.Devices.Client.Message(Encoding.ASCII.GetBytes("property message"))) // TODO: UTF8?
            // {
            //     // Send event but cancel after given timeout (OperationCanceledException)
            //     message.Properties["SendInterval"] = "44";
                
            //     await _deviceClient.SendEventAsync(message);
            //     //_log.Info(string.Format("{0} > Sending telemetry: {1}", DateTime.Now, messageString), () => {});
            //     //await sendTask;
            // }

            //using var message = new Microsoft.Azure.Devices.Client.Message(Encoding.UTF8.GetBytes("TestMessage"));
            //await _deviceClient.SendEventAsync(message);

            //await SendTelemetryAsync(payload, DateTime.Now, 4000);

            // await RetrieveDeviceTwin();

            
                //_deviceClient = DeviceClient.CreateFromConnectionString(connectionString);
                //_deviceClient.GetTwinAsync();
                // DigitalTwinClient twin = DigitalTwinClient.Create()

                //_registryManager = RegistryManager.Create(credentials.HubString, authMethod, Microsoft.Azure.Devices.Client.TransportType.Amqp);
                // _registryManager = RegistryManager.CreateFromConnectionString(
                //     "HostName=HHub.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=JM6aRNjmVDQUUQSzF/K1sngKqa39Rm5ZTTaHQ7QMcyg="
                // );

                // await _registryManager.GetDeviceAsync(credentials.DeviceId);
                // await _registryManager.GetTwinAsync(credentials.DeviceId);
                //_twin = await _registryManager.GetTwinAsync(credentials.DeviceId);

                // _deviceClient.SetConnectionStatusChangesHandler(OnConnectionStatusChange);
                

            // DigitalTwinClient digitalTwinClient = DigitalTwinClient.CreateFromConnectionString(credentials.HubString);
            // ModuleClient client = ModuleClient.CreateFromConnectionString(credentials.HubString);