package com.example;

import java.util.*;

import org.eclipse.californium.core.network.config.NetworkConfig;
// import org.eclipse.leshan.client.*;
import org.eclipse.leshan.client.californium.*;
import org.eclipse.leshan.client.resource.*;
import org.eclipse.leshan.core.*;
import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.StaticModel;
import org.eclipse.leshan.core.request.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.leshan.client.object.*;

// TODO: 
/**
 * - switch to TLV representation to minimize data
 */

public class LevelDeviceDemo {

    private static final Logger log = LoggerFactory.getLogger(LevelDeviceDemo.class);

    private static final int TELEMETRY_OBJECT_ID = 64000;
    private static final int PROPERTY_OBJECT_ID = 64001;
    private static final String ENDPOINT_ID = "LEV1";

    public static void main( String[] args )
    {
        LeshanClientBuilder builder = new LeshanClientBuilder(ENDPOINT_ID);
        NetworkConfig networkConfig = new NetworkConfig();

        try
        {
            LwM2mModel models = createModel();
            
            // create objects
            ObjectsInitializer initializer = new ObjectsInitializer(models);

            initializer.setInstancesForObject(LwM2mId.SECURITY, Security.noSec("coap://localhost:5683", 12345));
            initializer.setInstancesForObject(LwM2mId.SERVER, new Server(12345, 25000, EnumSet.of(BindingMode.U), false, BindingMode.U));// , BindingMode.U, false, BindingMode.U));
            //initializer.setInstancesForObject(LwM2mId.DEVICE, new Device());
            initializer.setInstancesForObject(LwM2mId.DEVICE, new DeviceObject("HOREICH", "LEV1-L476RG", "89882280666018939318")); // TODO: own device

            PropertyModule propertyModule = new PropertyModule(); // random property data generator
            PropertyObject propertyObject = new PropertyObject(propertyModule); // Tshe LwM2M object for properties
            PropertyObserver propertyObserver = new PropertyObserver(propertyObject, propertyModule);

            LevelTelemetryModule telemetryModule = new LevelTelemetryModule(); // random sensor data generator
            LevelTelemetryObject telemetryObject = new LevelTelemetryObject(telemetryModule);
            LevelTelemetryObserver telemetryObserver = new LevelTelemetryObserver(telemetryObject, telemetryModule);
            
            // Connect instances with models
            initializer.setInstancesForObject(PROPERTY_OBJECT_ID, propertyObject);
            initializer.setInstancesForObject(TELEMETRY_OBJECT_ID, telemetryObject);
            
            // initializer.setInstancesForObject(3303, new RandomTemperatureSensor());
            
            // Add it to the client
            // initializer.create(PROPERTY_OBJECT_ID, TELEMETRY_OBJECT_ID);

            List<LwM2mObjectEnabler> enablers = initializer.createAll();

            builder.setObjects(enablers);
            builder.setCoapConfig(networkConfig);
            LeshanClient client = builder.build();
            
            client.addObserver(propertyObserver);
            client.addObserver(telemetryObserver);

            client.start();
        }
        catch (Exception e)
        {
            log.error("Error: {}", e.getMessage());
        }
    }

    private static LwM2mModel createModel() throws Exception {

        String[] modelPaths = new String[] { "64000.xml", "64001.xml"};

        // Loads default objects (Security, Server, Device)
        List<ObjectModel> models = ObjectLoader.loadDefault();

        // Add custom models
        models.addAll(ObjectLoader.loadDdfResources("/customModels/", modelPaths));
        // if (cli.main.modelsFolder != null) {
        //     models.addAll(ObjectLoader.loadObjectsFromDir(cli.main.modelsFolder, true));
        // }
        return new StaticModel(models);
    }

    //private static lwM2mModel createModel(L)
}
