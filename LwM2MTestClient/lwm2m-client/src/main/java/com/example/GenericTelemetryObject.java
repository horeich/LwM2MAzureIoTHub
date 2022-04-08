package com.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.resource.ResourceChangedListener;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.Destroyable;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericTelemetryObject extends BaseInstanceEnabler implements Destroyable {

    private static final Logger LOG = LoggerFactory.getLogger(GenericTelemetryObject.class);

    private static final String UNIT_CELSIUS = "cel";
    private static final int TEMPERATURE_VALUE = 5700;
    private static final int HUMIDITY_VALUE = 5701;

    private static final int MAX_MEASURED_VALUE = 5602;
    private static final int MIN_MEASURED_VALUE = 5601;
    private static final int RESET_MIN_MAX_MEASURED_VALUES = 5605;

    private static final List<Integer> supportedResources = 
        Arrays.asList(TEMPERATURE_VALUE, HUMIDITY_VALUE, MAX_MEASURED_VALUE,
            MIN_MEASURED_VALUE, RESET_MIN_MAX_MEASURED_VALUES);

    private final ScheduledExecutorService scheduler;
    private final Random rng = new Random();
    private float currentTemp = 0.0f;
    private float humidity = 0.0f;
    private double minMeasuredValue = currentTemp;
    private double maxMeasuredValue = currentTemp;

    public GenericTelemetryObject() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Temperature Sensor"));
        // scheduler.scheduleAtFixedRate(new Runnable() {

        //     @Override
        //     public void run() {
        //         adjustTemperature();
        //     }
        // }, 20, 20, TimeUnit.SECONDS);
    }

    @Override
    public synchronized ReadResponse read(ServerIdentity identity, int resourceId) {
        LOG.info("Read on Temperature resource /{}/{}/{}", getModel().id, getId(), resourceId);
        switch (resourceId) {
            case MIN_MEASURED_VALUE:
                return ReadResponse.success(resourceId, getTwoDigitValue(minMeasuredValue));
            case MAX_MEASURED_VALUE:
                return ReadResponse.success(resourceId, getTwoDigitValue(maxMeasuredValue));

            case TEMPERATURE_VALUE:
                float temp = getTwoDigitValue(currentTemp);
                LOG.info("Get current Temperature {}: ", temp);
                return ReadResponse.success(resourceId, temp);
            case HUMIDITY_VALUE:
                float hum = getTwoDigitValue(this.humidity);
                LOG.info("Get current Humidity {}: ", hum);
                return ReadResponse.success(resourceId, hum);

            // case UNITS:
            //     return ReadResponse.success(resourceId, UNIT_CELSIUS);
            default:
                return super.read(identity, resourceId);
        }
    }

    /**
     * if observe =/64000 - observe is called for each resource id - 
     * The observe message is built from multiple reads
     */
    @Override
    public ObserveResponse observe(ServerIdentity identity) {
        // Perform a read by default

        // Calls base method that adds single resources to a LwM2mObjectInstance
        ReadResponse readResponse = this.read(identity);
        return new ObserveResponse(readResponse.getCode(), readResponse.getContent(), null, null,
                readResponse.getErrorMessage());
    }

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
     */
    public void notifyResourcesChanged(int... resourceIds) {
        for (ResourceChangedListener listener : listeners) {

            // Depending on single or multiple resource a different observe method is called
            listener.resourcesChanged(resourceIds);
        }
    }

    @Override
    public synchronized ExecuteResponse execute(ServerIdentity identity, int resourceId, String params) {
        LOG.info("Execute on Temperature resource /{}/{}/{}", getModel().id, getId(), resourceId);
        switch (resourceId) {
            case RESET_MIN_MAX_MEASURED_VALUES:
                resetMinMaxMeasuredValues();
                return ExecuteResponse.success();
            default:
                return super.execute(identity, resourceId, params);
        }
    }

    private float getTwoDigitValue(double value) {
        BigDecimal toBeTruncated = BigDecimal.valueOf(value);
        return toBeTruncated.setScale(2, RoundingMode.HALF_UP).floatValue();
    }

    private void adjustTemperature() {
        float delta = (rng.nextInt(20) - 10) / 10f;
        currentTemp += delta;
        Integer changedResource = adjustMinMaxMeasuredValue(currentTemp);
        if (changedResource != null) {
            notifyResourcesChanged(TEMPERATURE_VALUE, changedResource);
        } else {
            notifyResourcesChanged(TEMPERATURE_VALUE);
        }
    }

    private synchronized Integer adjustMinMaxMeasuredValue(double newTemperature) {
        if (newTemperature > maxMeasuredValue) {
            maxMeasuredValue = newTemperature;
            return MAX_MEASURED_VALUE;
        } else if (newTemperature < minMeasuredValue) {
            minMeasuredValue = newTemperature;
            return MIN_MEASURED_VALUE;
        } else {
            return null;
        }
    }

    private void resetMinMaxMeasuredValues() {
        minMeasuredValue = currentTemp;
        maxMeasuredValue = currentTemp;
    }

    public final void SetTemperature(float temperature)
    {
        this.currentTemp = temperature;
    }

    public final void SetHumidity(float humidity)
    {
        this.humidity = humidity;
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }

    @Override
    public void destroy() {
        scheduler.shutdown();
    }
}
