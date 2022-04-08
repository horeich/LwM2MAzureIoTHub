// Copyright (c) Microsoft. All rights reserved.

package app.com.horeich.services.runtime;

/**
 * Service layer configuration
 */
public class ServicesConfig implements IServicesConfig {

    private String azureMapsKey;
    private String seedTemplate;
    private String storageAdapterApiUrl;
    private String deviceSimulationApiUrl;
    private String telemetryUrl;
    private String userManagementApiUrl;
    private String registrationApiUrl;
    //private IActionsConfig actionsConfig;

    public ServicesConfig() {
    }

    public ServicesConfig(String telemetryUrl,
                          String storageAdapterApiUrl,
                          String userManagementApiUrl,
                          String deviceSimulationApiUrl,
                          String seedTemplate,
                          String azureMapsKey,
                          String registrationApiUrl) {
                          //IActionsConfig actionsConfig) {
        this.storageAdapterApiUrl = storageAdapterApiUrl;
        this.deviceSimulationApiUrl = deviceSimulationApiUrl;
        this.userManagementApiUrl = userManagementApiUrl;
        this.seedTemplate = seedTemplate;
        this.telemetryUrl = telemetryUrl;
        this.azureMapsKey = azureMapsKey;
        this.userManagementApiUrl = userManagementApiUrl;
        this.registrationApiUrl = registrationApiUrl;
        //this.actionsConfig = actionsConfig;
    }

    @Override
    public String getAzureMapsKey() {
        return azureMapsKey;
    }

    public void setAzureMapsKey(String azureMapsKey) {
        this.azureMapsKey = azureMapsKey;
    }

    @Override
    public String getTelemetryUrl() {
        return telemetryUrl;
    }

    public void setTelemetryApiUrl(String telemetryApiUrl) {
        this.telemetryUrl = telemetryUrl;
    }


    @Override
    public String getSeedTemplate() {
        return seedTemplate;
    }

    @Override
    public String getStorageAdapterApiUrl() {
        return storageAdapterApiUrl;
    }

    public void setStorageAdapterApiUrl(String storageAdapterApiUrl) {
        this.storageAdapterApiUrl = storageAdapterApiUrl;
    }

    @Override
    public String getDeviceSimulationApiUrl() {
        return this.deviceSimulationApiUrl;
    }

    public void setDeviceSimulationApiUrl(String deviceSimulationApiUrl) {
        this.deviceSimulationApiUrl = deviceSimulationApiUrl;
    }

    @Override
    public String getUserManagementApiUrl() {
        return this.userManagementApiUrl;
    }

    @Override
    public String getRegistrationUrl() {
        return this.registrationApiUrl;
    }

    // @Override
    // public IActionsConfig getActionsConfig() {
    //     return this.actionsConfig;
    // }
}