package com.example;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.californium.elements.util.NamedThreadFactory;
import org.eclipse.leshan.client.observer.LwM2mClientObserverAdapter;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.request.RegisterRequest;
import org.eclipse.leshan.core.request.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelemetryObserver extends LwM2mClientObserverAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyObserver.class);
    
    private final Random rng = new Random();

    private TelemetryModule telemetryModule;
    private TelemetryObject telemetryObject;

    public TelemetryObserver(TelemetryObject telemetryObject, TelemetryModule telemetryModule) {
        this.telemetryObject = telemetryObject;
        this.telemetryModule = telemetryModule;
    }

    @Override
    public void onRegistrationSuccess(ServerIdentity server, RegisterRequest request, String registrationID) {
        LOG.info("Server singals successful registration -> waiting for observe request");
    }

    /**
     * After a successful update CHANGED telemetry is sent to the server via NOTIFY
     */
    @Override
    public void onUpdateSuccess(ServerIdentity server, UpdateRequest request) {

        LOG.info("Server singals successful update -> notify changed telemetry");

        telemetryModule.measureAll(); // take pseudo measurements

        // ArrayList<Integer> changedResources = new ArrayList<Integer>();
        // if (telemetryModule.hasTemperatureChanged)
        // {
        //     changedResources.add(telemetryObject.getTemperatureId());
        // }
        // if (telemetryModule.hashumidityChanged)
        // {
        //     changedResources.add(telemetryObject.getHumidityId());
        // }
        // if (telemetryModule.hasRadioactivityChanged)
        // {
        //     changedResources.add(telemetryObject.getRadioactivityId());
        // }
        if (telemetryModule.hasTelemetryChanged())
        {
            // changedResources.add(telemetryObject.getTimestampId());
            // telemetryObject.setChangedResource(changedResources);
            
            // Enable notify resource changed with dummy value
            // see: https://stackoverflow.com/questions/718554/how-to-convert-an-arraylist-containing-integers-to-primitive-int-array
            this.telemetryObject.notifyResourcesChanged(1,2,3);//changedResources.stream().mapToInt(Integer::intValue).toArray());
        }
    }
}
