package com.example;

import org.eclipse.leshan.client.resource.*;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinaryDataObject extends BaseInstanceEnabler {
    
    private static final Logger LOG = LoggerFactory.getLogger(BinaryDataObject.class);

    private String hexString = new String("");

    public BinaryDataObject() {
        
    }

    @Override
    public ReadResponse read(ServerIdentity identity, int resourceid) {
        LOG.info("Read on Location resource /{}/{}/{}", getModel().id, getId(), resourceid);
        //switch (resourceid) {
        // case 0:
        //     return ReadResponse.success(resourceid, getLatitude());
        // case 1:
        //     return ReadResponse.success(resourceid, getLongitude());
        // case 5:
        //     return ReadResponse.success(resourceid, getTimestamp());
        // default:
        return super.read(identity, resourceid);
        //}
    }

    @Override
    public ObserveResponse observe(ServerIdentity identity, int resourceid) {

        // Perform a read by default
        ReadResponse readResponse = this.read(identity, resourceid);
        return new ObserveResponse(readResponse.getCode(), readResponse.getContent(), null, null,
                readResponse.getErrorMessage());
    }

}
