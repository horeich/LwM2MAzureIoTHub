package com.example;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.leshan.core.node.LwM2mResource;

public class PropertyModule {
    
    private static final Logger LOG = LoggerFactory.getLogger(PropertyModule.class);

    private final Random rng = new Random();

    private Long sendInterval = 3600L;
    public volatile boolean hasSendIntervalChanged = false;

    private Long reconnectAttemps = 3L;
    public volatile boolean hasReconnectAttempsChanged = false;

    private Long networkDisabledTime = 7200L;
    public volatile boolean hasNetworkDisabledTimeChanged = false;

    // private final ScheduledExecutorService scheduler;
    // TODO: change properties in irregular intervals
    // TODO: mutex?

    public PropertyModule() {
        // scheduler.scheduleAtFixedRate(new Runnable() {

        //     @Override
        //     public void run() {
        //         changeRandomProperty
        //     }
        // }, 50, 50, TimeUnit.SECONDS); // every 60 seconds
    }

    public Long getSendInterval() {
        hasSendIntervalChanged = false;
        return this.sendInterval;
    }

    public Long getReconnectAttemps() {
        hasReconnectAttempsChanged = false;
        return this.reconnectAttemps;
    }

    public Long getNetworkDisabledTime() {
        hasNetworkDisabledTimeChanged = false;
        return this.networkDisabledTime;
    }

    public void adjustSendInterval() {
        int delta = (rng.nextInt(20) - 10);
        setSendInterval(this.sendInterval += delta);
    }

    public void adjustNetworkDisabledTime() {
        int delta = (rng.nextInt(20) - 10);
        setNetworkDisabledTime(this.networkDisabledTime += delta);
    }

    public void adjustReconnectAttemps() {
        int delta = (rng.nextInt(2) - 1);
        setReconnectAttempts(this.reconnectAttemps += delta);
    }

    public void setSendInterval(Long sendInterval) {
        this.sendInterval = sendInterval;
        this.hasSendIntervalChanged = true;
        LOG.info("Updated \"SendInterval\" to {}", sendInterval);
    }

    public void setReconnectAttempts(Long reconnectAttemps)  {
        this.reconnectAttemps = reconnectAttemps;
        this.hasReconnectAttempsChanged = true;
        LOG.info("Updated \"ReconnectAttemps\" to {}", reconnectAttemps);
    }

    public void setNetworkDisabledTime(Long networkDisabledTime) {
        this.networkDisabledTime = networkDisabledTime;
        this.hasNetworkDisabledTimeChanged = true;
        LOG.info("Updated \"NetworkDisabledTime\" to {}", networkDisabledTime);
    }
}
