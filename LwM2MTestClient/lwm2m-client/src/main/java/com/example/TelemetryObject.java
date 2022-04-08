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

public class TelemetryObject extends BaseInstanceEnabler implements Destroyable {
    
    private static final Logger LOG = LoggerFactory.getLogger(TelemetryObject.class);

    private static final int TEMPERATURE_ID = 1;
    private static final int HUMIDITY_ID = 2;
    private static final int WATER_LEVEL_ID = 4;
    private static final int TIMESTAMP_ID = 3;

    private TelemetryModule telemetryModule;

    // private List<Integer> changedResourceIds;

    // Must contain a default constructor
    public TelemetryObject() {

    }
    
    public TelemetryObject(TelemetryModule telemetryModule) {
        this.telemetryModule = telemetryModule;
        //this.changedResourceIds = new ArrayList<>();
    }

    public int getHumidityId() {
        return HUMIDITY_ID;
    }

    public int getTemperatureId() {
        return TEMPERATURE_ID;
    }

    public int getTimestampId() {
        return TIMESTAMP_ID;
    }

    public int getWaterLevelId() {
        return WATER_LEVEL_ID;
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
            case TEMPERATURE_ID:
                return ReadResponse.success(resourceId, telemetryModule.getTemperature());
            case HUMIDITY_ID:
                return ReadResponse.success(resourceId, telemetryModule.getHumidity());
            case WATER_LEVEL_ID:
                return ReadResponse.success(resourceId, telemetryModule.getWaterLevel());
            case TIMESTAMP_ID:
                return ReadResponse.success(resourceId, telemetryModule.getTimeStamp());
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
        
        if (telemetryModule.hasTemperatureChanged)
        {
            ReadResponse response = read(identity, TEMPERATURE_ID);
            resources.add((LwM2mResource)response.getContent());
        }
        if (telemetryModule.hashumidityChanged)
        {
            ReadResponse response = read(identity, HUMIDITY_ID);
            resources.add((LwM2mResource)response.getContent());
        }
        if (telemetryModule.hasWaterLevelChanged)
        {
            ReadResponse response = read(identity, WATER_LEVEL_ID);
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

