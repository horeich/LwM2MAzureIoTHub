// Copyright (c) Microsoft. All rights reserved.

package app.com.horeich.services.runtime;

public interface IServicesConfig {

    String getAzureMapsKey();

    String getSeedTemplate();

    String getStorageAdapterApiUrl();

    String getDeviceSimulationApiUrl();

    String getTelemetryUrl();

    String getUserManagementApiUrl();

    String getRegistrationUrl();

    //IActionsConfig getActionsConfig();
}
