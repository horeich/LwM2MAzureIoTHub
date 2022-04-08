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

public class TelemetryModule {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyModule.class);

    private final Random rng = new Random();
    private final ScheduledExecutorService scheduler;

    private double temperature = 20.0f;
    public volatile boolean hasTemperatureChanged = true; // set true @startup

    private double humidity = 60.0f;
    public volatile boolean hashumidityChanged = true;

    private long waterLevel = 100;
    public volatile boolean hasWaterLevelChanged = true;

    private Instant timeStamp = Instant.now();

    // TODO: change properties in irregular intervals
    // TODO: mutex?

    public TelemetryModule() {
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

    public double getTemperature() {
        hasTemperatureChanged= false;
        LOG.info("Fetched current temperature");
        return this.temperature;
    }

    public double getHumidity() {
        hashumidityChanged = false;
        LOG.info("Fetched current humidity");
        return this.humidity;
    }

    public long getWaterLevel() {
        hasWaterLevelChanged = false;
        LOG.info("Fetched current water level");
        return this.waterLevel;
    }

    public boolean hasTelemetryChanged()
    {
        return (hasTemperatureChanged && hashumidityChanged && hasWaterLevelChanged);
    }

    private void adjustTemperature() {
        double delta = (rng.nextInt(20) - 10) / 10.0f;
        this.temperature += delta;
        hasTemperatureChanged = true;
        LOG.info("Updated \"Temperature\" to {}", this.temperature);
    }

    private void adjustHumidity() {
        double delta = (rng.nextInt(20) - 10) / 10.0f;
        this.humidity += delta;
        hashumidityChanged = true;
        LOG.info("Updated \"Humidity\" to {}", this.humidity);
    }

    public void measureAll() {
        LOG.info("Perform fake measurements");
        adjustTemperature();
        adjustHumidity();
        this.timeStamp = Instant.now(); // time of measurements
        hasTemperatureChanged = true;
        hashumidityChanged = true;
        hasWaterLevelChanged = true;
    }
}
