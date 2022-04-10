package com.example;

import java.util.Random;

import javax.security.auth.Destroyable;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.resource.ResourceChangedListener;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.codec.senml.LwM2mResolvedSenMLRecord;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyObject extends BaseInstanceEnabler implements Destroyable {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyObject.class);

    private final Random rng = new Random();

    private static final int SEND_INTERVAL_ID = 1;
    private static final int RECONNECT_ATTEMPTS_ID = 2;
    //private static final int NETWORK_DISABLED_TIME_ID = 3;

    private PropertyModule propertyModule;

    // Must contain a default constructor
    public PropertyObject() {

    }

    public PropertyObject(PropertyModule propertyModule) {
        this.propertyModule = propertyModule;
    }

    public int getSendIntervalId() {
        return SEND_INTERVAL_ID;
    }

    public int getReconnectAttemptsId() {
        return RECONNECT_ATTEMPTS_ID;
    }

    // public int getNetworkDisbledTimeId() {
    //     return NETWORK_DISABLED_TIME_ID;
    // }

    /**
     * Called when there was a write on an object or resource
     * Note: Integer type decodes as Long
     */
    @Override
    public WriteResponse write(ServerIdentity identity, boolean replace, int resourceid, LwM2mResource value) {
        LOG.info("Write on Property Resource /{}/{}/{}", getModel().id, getId(), resourceid);
        switch (resourceid) {
            case SEND_INTERVAL_ID:
                Long sendInterval = (Long)value.getValue();
                propertyModule.setSendInterval(sendInterval);
                return WriteResponse.success(); // we cannot send content on write
            case RECONNECT_ATTEMPTS_ID:
                Long reconnectAttemps = (Long) value.getValue();
                propertyModule.setReconnectAttempts(reconnectAttemps);
                return WriteResponse.success(); // we cannot send content on write
            // case NETWORK_DISABLED_TIME_ID:
            //     Long networkDisabledTime = (Long) value.getValue();
            //     propertyModule.setNetworkDisabledTime(networkDisabledTime);
            //     return WriteResponse.success(); // we cannot send content on write
            default:
                return super.write(identity, replace, resourceid, value);
        }
    }

    /**
     * This method is called from super class BaseInstanceEnabler.read and adds all
     * resources
     */
    @Override
    public synchronized ReadResponse read(ServerIdentity identity, int resourceId) {
        LOG.info("Read on Property resource /{}/{}/{}", getModel().id, getId(), resourceId);

        switch (resourceId) {
            case SEND_INTERVAL_ID:
                return ReadResponse.success(resourceId, propertyModule.getSendInterval());
            case RECONNECT_ATTEMPTS_ID:
                return ReadResponse.success(resourceId, propertyModule.getReconnectAttemps());
            // case NETWORK_DISABLED_TIME_ID:
            //     return ReadResponse.success(resourceId, propertyModule.getNetworkDisabledTime());
            default:
                return super.read(identity, resourceId);
        }
    }

    // @Override
    // public ReadResponse read(ServerIdentity identity) {
    //     List<LwM2mResource> resources = new ArrayList<>();
    //     for (ResourceModel resourceModel : model.resources.values()) {
    //         // check, if internal request (SYSTEM) or readable
    //         if (identity.isSystem() || resourceModel.operations.isReadable()) {
    //             ReadResponse response = read(identity, resourceModel.id);
    //             if (response.isSuccess() && response.getContent() instanceof LwM2mResource)
    //                 resources.add((LwM2mResource) response.getContent());
    //         }
    //     }
    //     return ReadResponse.success(new LwM2mObjectInstance(id, resources));
    // }

    /**
     * Called when a object is requested for observation
     * Note: Report all current property values on observation request to sync
     * device info on server
     */
    @Override
    public ObserveResponse observe(ServerIdentity identity) {


        // Perform a read by default
        // Calls base method that adds single resources to a LwM2mObjectInstance
        ReadResponse readResponse = this.read(identity);

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
