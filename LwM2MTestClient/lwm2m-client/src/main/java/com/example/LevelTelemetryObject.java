package com.example;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Destroyable;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.resource.ResourceChangedListener;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelTelemetryObject extends BaseInstanceEnabler implements Destroyable {
    
    private static final Logger LOG = LoggerFactory.getLogger(LevelTelemetryObject.class);

    private static final int TIMESTAMP_ID = 1;
    private static final int DEVICE_TEMPERATURE_ID = 2;
    private static final int WATER_TEMPERATURE_ID = 3;
    private static final int WATER_PRESSURE_ID = 4;

    private LevelTelemetryModule telemetryModule;

    // private List<Integer> changedResourceIds;

    // Must contain a default constructor
    public LevelTelemetryObject() {

    }
    
    public LevelTelemetryObject(LevelTelemetryModule telemetryModule) {
        this.telemetryModule = telemetryModule;
        //this.changedResourceIds = new ArrayList<>();
    }

    public int getTimeStampId() {
        return TIMESTAMP_ID;
    }

    public int getDeviceTemperatureId() {
        return DEVICE_TEMPERATURE_ID;
    }

    public int getWaterTemperatureId() {
        return WATER_TEMPERATURE_ID;
    }

    public int getWaterPressureId() {
        return WATER_PRESSURE_ID;
    }

    // public void setChangedResource(ArrayList<Integer> changedResources)
    // {
    //     this.changedResourceIds = changedResources;
    // }

    /**
     * Called when there was a write on an object or resource
     */
    @Override
    public WriteResponse write(ServerIdentity identity, boolean replace, int resourceid, LwM2mResource value) {
        LOG.info("Write on Telemtry Resource /{}/{}/{}", getModel().id, getId(), resourceid);
        return super.write(identity, replace, resourceid, value);
    }

    /**
     * This method is called from super class BaseInstanceEnabler.read and adds all
     * resources
     */
    @Override
    public synchronized ReadResponse read(ServerIdentity identity, int resourceId) {
        LOG.info("Read on Telemetry Resource /{}/{}/{}", getModel().id, getId(), resourceId);

        switch (resourceId) {
            case TIMESTAMP_ID:
                return ReadResponse.success(resourceId, telemetryModule.getTimeStamp());
            case DEVICE_TEMPERATURE_ID:
                return ReadResponse.success(resourceId, telemetryModule.getDeviceTemperature());
            case WATER_TEMPERATURE_ID:
                return ReadResponse.success(resourceId, telemetryModule.getWaterTemperature());
            case WATER_PRESSURE_ID:
                return ReadResponse.success(resourceId, telemetryModule.getWaterPressure());
            default:
                return super.read(identity, resourceId);
        }
    }

    /**
     * Called when a object is requested for observation
     * Note: Report all current property values on observation request to sync
     * device info on server
     */
    @Override
    public ObserveResponse observe(ServerIdentity identity) {

        // On observe request do pseudo measurements
        //telemetryModule.measureAll();

        // Perform a read by default
        // Calls base method that adds single resources to a LwM2mObjectInstance
        // List<LwM2mResource> resources = new ArrayList<>();
        // for (ResourceModel resourceModel : model.resources.values()) {
        //     // check, if internal request (SYSTEM) or readable
        //     if (identity.isSystem() || resourceModel.operations.isReadable()) {
        //         ReadResponse response = read(identity, resourceModel.id);
        //         if (response.isSuccess() && response.getContent() instanceof LwM2mResource)
        //             resources.add((LwM2mResource) response.getContent());
        //     }
        // }
        List<LwM2mResource> resources = new ArrayList<>();
        if (telemetryModule.hasDeviceTemperatureChanged)
        {
            ReadResponse response = read(identity, DEVICE_TEMPERATURE_ID);
            resources.add((LwM2mResource)response.getContent());
        }
        if (telemetryModule.hasWaterTemperatureChanged)
        {
            ReadResponse response = read(identity, WATER_TEMPERATURE_ID);
            resources.add((LwM2mResource)response.getContent());
        }
        if (telemetryModule.hasWaterPressureChanged)
        {
            ReadResponse response = read(identity, WATER_PRESSURE_ID);
            resources.add((LwM2mResource)response.getContent());
        }

        ReadResponse response = read(identity, TIMESTAMP_ID);
        resources.add((LwM2mResource)response.getContent());

        ReadResponse readResponse = ReadResponse.success(new LwM2mObjectInstance(this.id, resources));
        //ReadResponse readResponse = this.read(identity);
        
        // Generates observe response with read content
        return new ObserveResponse(readResponse.getCode(), readResponse.getContent(), null, null,
                readResponse.getErrorMessage());
    }

    /**
     * Called when a single resource is requested for observation
     */
    @Override
    public ObserveResponse observe(ServerIdentity identity, int resourceid) {

        // Perform a read by default
        ReadResponse readResponse = this.read(identity, resourceid);
        return new ObserveResponse(readResponse.getCode(), readResponse.getContent(), null, null,
                readResponse.getErrorMessage());
    }

    /*
     * To be used to notify that 1 or several resources change. <p> This method
     * SHOULD NOT be called in a synchronize block or any thread synchronization
     * tools to avoid any risk of deadlock. <p> Calling this method is needed to
     * trigger NOTIFICATION when an observe relation is established.
     * 
     * @param resourceIds the list of resources which change.
     * 
     * Update single or multiple resources
     * 
     */
    public void notifyResourcesChanged(int... resourceIds) {
        for (ResourceChangedListener listener : listeners) {
            listener.resourcesChanged(resourceIds);
        }
    }

}

