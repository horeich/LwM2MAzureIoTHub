package com.example;

import java.sql.Time;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.leshan.core.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelTelemetryModule {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyModule.class);

    private final Random rng = new Random();
    private final ScheduledExecutorService scheduler;

    private double waterTemperature = 20.0321;
    public volatile boolean hasWaterTemperatureChanged = true; // set true @startup

    private double deviceTemperature = 60.896;
    public volatile boolean hasDeviceTemperatureChanged = true;

    private double waterPressure = 100.01233;
    public volatile boolean hasWaterPressureChanged = true;

    private Instant timeStamp = Instant.now();

    // TODO: change properties in irregular intervals
    // TODO: mutex?

    public LevelTelemetryModule() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Telemetry executor"));
        // scheduler.scheduleAtFixedRate(new Runnable() {

        // @Override
        // public void run() {
        // adjustTemperature();
        // adjustHumidity();
        // }
        // }, 60, 60, TimeUnit.SECONDS); // every 60 seconds
    }

    public Date getTimeStamp() {
        LOG.info("Fetched current timestamp");
        return Date.from(timeStamp);
    }

    public double getDeviceTemperature() {
        hasDeviceTemperatureChanged = false;
        LOG.info("Fetched current device temperature");
        return this.deviceTemperature;
    }

    public double getWaterTemperature() {
        hasWaterTemperatureChanged= false;
        LOG.info("Fetched current water temperature");
        return this.waterTemperature;
    }

    public double getWaterPressure() {
        hasWaterPressureChanged = false;
        LOG.info("Fetched current water pressure");
        return this.waterPressure;
    }

    public boolean hasTelemetryChanged()
    {
        return (hasWaterTemperatureChanged && hasDeviceTemperatureChanged && hasWaterPressureChanged);
    }

    private void adjustDeviceTemperature() {
        double delta = (rng.nextInt(20) - 10) / 10.0f;
        this.waterTemperature += delta;
        hasWaterTemperatureChanged = true;
        LOG.info("Updated \"DeviceTemperature\" to {}", this.waterTemperature);
    }

    private void adjustWaterTemperature() {
        double delta = (rng.nextInt(20) - 10) / 10.0f;
        this.waterTemperature += delta;
        hasWaterTemperatureChanged = true;
        LOG.info("Updated \"WaterTemperature\" to {}", this.waterTemperature);
    }

    private void adjustWaterPressure() {
        double delta = (rng.nextInt(20) - 10) / 10.0f;
        this.deviceTemperature += delta;
        hasDeviceTemperatureChanged = true;
        LOG.info("Updated \"WaterPressure\" to {}", this.deviceTemperature);
    }

    public void measureAll() {
        LOG.info("Perform fake measurements");
        adjustWaterTemperature();
        adjustWaterPressure();
        this.timeStamp = Instant.now(); // time of measurements
        hasWaterTemperatureChanged = true;
        hasDeviceTemperatureChanged = true;
        hasWaterPressureChanged = true;
    }
}
