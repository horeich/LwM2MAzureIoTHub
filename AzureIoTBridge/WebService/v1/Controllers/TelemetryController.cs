﻿using System;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using System.Collections.Generic;

using System.Text.Encodings.Web;
using System.Web;
using System.Net;
using Microsoft.Azure.Devices.Client;

using Horeich.SensingSolutions.Services.Diagnostics;
using Horeich.SensingSolutions.Services.VirtualDevice;
using Horeich.Services.v1.Models;

namespace Horeich.IoTBridge.v1.Controllers
{
    [ApiController]
    [Route(Version.PATH)]
    public class TelemetryController : ControllerBase
    {
        private readonly ILogger _log;
        private readonly IVirtualDeviceManager _deviceManager;

        /// <summary>
        /// Constructor
        /// </summary>
        /// <param name="logger"></param>
        public TelemetryController(
            IVirtualDeviceManager deviceManager,
            ILogger logger)
        {
            _deviceManager = deviceManager;
            _log = logger;     
        }

        /// <summary>
        /// Receive device 
        /// </summary>
        /// <param name="deviceId"></param>
        /// <param name="telemetry"></param>
        /// <returns></returns>
        // [HttpPut("{deviceId}/telemetry/bin")]
        // public async Task<IActionResult> PutAsync(string deviceId, [FromQuery]BinaryTelemetryApiModel binaryTelemetry)
        // {

        //     // RETURN HTTP ERROR when fail
        //     await _deviceManager.SolveBinaryTelemetry(deviceId, binaryTelemetry);

        //     // the data has reached its destination -> return Created
        //     return new CreatedResult("IoTBridge", deviceId);
        // }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="deviceId"></param>
        /// <param name="telemetry"></param>
        /// <returns></returns>
        [HttpPut("{deviceId}/telemetry")]
        public async Task<IActionResult> PutAsync(string deviceId, [FromBody]TelemetryApiModel telemetry)
        {
            await _deviceManager.SendTelemetryAsync(deviceId, telemetry.ToServiceModel());
            return new CreatedResult("IoTBridge", deviceId);
        }

      

        // [HttpPut("{deviceId}/{data}/telemetry")]
        // public async Task<IActionResult> PutAsync(string deviceId, string data, [FromQuery]TelemetryApiModel telemetry)
        // {
        //     // Send data asynchronously
        //     await _deviceManager.SolveTelemetry(deviceId, telemetry);

        //     // the data has reached its destination -> return Created
        //     return new CreatedResult("IoTBridge", deviceId);
        // }
    }
}















//         // public IEnumerable<WeatherForecast> Get()
//         // {
//         //     var rng = new Random();
//         //     return Enumerable.Range(1, 5).Select(index => new WeatherForecast
//         //     {
//         //         Date = DateTime.Now.AddDays(index),
//         //         TemperatureC = rng.Next(-20, 55),
//         //         Summary = Summaries[rng.Next(Summaries.Length)]
//         //     })
//         //     .ToArray();
//         // }
//         // [HttpGet("[controller]/[action]")]
//         // public string SendDeviceData()
//         // {
//         //     string name = "Rick";
//         //     var numTimes = 2;
//         //     Console.WriteLine("Processing HttpGet request...");
//         //     return HtmlEncoder.Default.Encode($"Hello {name}, NumTimes is: {numTimes}");
//         // }

//         // /// <summary>
//         // /// 
//         // /// </summary>
//         // /// <param name="str"></param>
//         // /// <returns></returns>
// /// 
//         // [HttpPost]
//         // public async Task<DeviceLink> AsyncSendData()
//         // {
//         //     string scopeID ="2", deviceID = "3", primaryKey = "4";
//         //     Task<DeviceLink> task = DeviceLink.CreateAsync(scopeID, deviceID, primaryKey);
//         //     DeviceLink link = await task;
//         //     // DeviceLink link = task.Result;
//         //     return link;
//         // }

//         [HttpGet("[controller]/[action]")]
//         public string TestConnection()
//         {
//             string name = "Andy";
//             return HtmlEncoder.Default.Encode($"Hello {name}, sent from IoT link");
//         }

//         // [HttpPut("[controller]/[action]")]
//         // public void AsyncSendDataPut(string id, string sId, string pK)
//         // {
//         //     //await _deviceLink.AuthenticateDeviceAsync(id, sId, pK);

//         //     //await _deviceLink.SendDeviceTelemetryAsync(client, "test");
//         //    // return client;
//         // }

//         // [HttpGet("[action]")]
//         // public string GetList([ModelBinder]List<string> id)
//         // {
//         //     return string.Join(",", id);
//         // }