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

public class PropertyObserver extends LwM2mClientObserverAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyObserver.class);

    private final ScheduledExecutorService scheduler;
    private final Random rng = new Random();

    private PropertyModule propertyModule;
    private PropertyObject propertyObject;

    public PropertyObserver(PropertyObject propertyObject, PropertyModule propertyModule) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Generic sensor"));
        this.propertyObject = propertyObject;
        this.propertyModule = propertyModule;
    }

    @Override
    public void onRegistrationSuccess(ServerIdentity server, RegisterRequest request, String registrationID) {
        LOG.info("Server singals successful registration -> waiting for observe request");
    }

    /**
     * After a successful update CHANGED properties are sent to the server via NOTIFY
     */
    @Override
    public void onUpdateSuccess(ServerIdentity server, UpdateRequest request) {

        // LOG.info("Server singals successful update -> notify changed properties");
        
        // // Set fake
        // propertyModule.setSendInterval(3000L);

        // ArrayList<Integer> changedResources = new ArrayList<Integer>();

        // if (propertyModule.hasSendIntervalChanged)
        // {
        //     changedResources.add(propertyObject.getSendIntervalId());
        // }
        // if (propertyModule.hasReconnectAttempsChanged)
        // {
        //     changedResources.add(propertyObject.getReconnectAttemptsId());
        // }
        // if (propertyModule.hasNetworkDisabledTimeChanged)
        // {
        //     changedResources.add(propertyObject.getNetworkDisbledTimeId());
        // }
        // if (changedResources.size() > 0)
        // {
        //     // see: https://stackoverflow.com/questions/718554/how-to-convert-an-arraylist-containing-integers-to-primitive-int-array
        //     this.propertyObject.notifyResourcesChanged(changedResources.stream().mapToInt(Integer::intValue).toArray());
        // }

        // scheduler.execute(new Runnable() {
        
        //     @Override
        //     public void run() {
        //         sendGenericData();
        //     }
        // });
    }
}
