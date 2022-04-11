package com.example;

import java.util.HashMap;
import java.util.List;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectivityMonitoringObject extends BaseInstanceEnabler {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceObject.class);

    private static final int NETWORK_BEARER_ID = 0;
    private static final int AVAILABLE_NETWORK_BEARER_ID = 1;
    private static final int RADIO_SIGNAL_STRENGTH_ID = 2;
    private static final int CELL_ID_ID = 8;
    

    @Override
    public ReadResponse read(ServerIdentity identity, int resourceId) {
        if (!identity.isSystem()) {
            LOG.info("Read on Connectivity Monitoring resource /{}/{}/{}", getModel().id, getId(), resourceId);
        }
        switch (resourceId) {
            case NETWORK_BEARER_ID:
                return ReadResponse.success(resourceId, getNetworkBearer());
            // case AVAILABLE_NETWORK_BEARER_ID:
            //     return ReadResponse.success(resourceId, getAvailableNetworkBearer());
            case RADIO_SIGNAL_STRENGTH_ID:
                return ReadResponse.success(resourceId, getRadioSignalStrength());
            case CELL_ID_ID:
                return ReadResponse.success(resourceId, getCellId());
            default:
                return super.read(identity, resourceId);
        }
    }

    @Override
    public ObserveResponse observe(ServerIdentity identity) {
        // Perform a read by default
        ReadResponse readResponse = this.read(identity);
        return new ObserveResponse(readResponse.getCode(), readResponse.getContent(), null, null,
                readResponse.getErrorMessage());
    }

    private int getNetworkBearer()
    {
        return 28201;
    }

    private int getRadioSignalStrength()
    {
        return -67;
    }

    private int getCellId()
    {
        return 0x0FFF;
    }

    // List<Integer> getAvailableNetworkBearer()
    // {
    //     List<Integer> plmns = new List<Integer>();
    //     plmns.add(28201);
    //     plmns.add(28203);
    //     plmns.add(28203);
    //     return plmns;
    // }
}
