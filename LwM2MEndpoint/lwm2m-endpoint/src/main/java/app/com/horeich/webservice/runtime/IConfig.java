// Copyright (c) Microsoft. All rights reserved.

package app.com.horeich.webservice.runtime;

import com.google.inject.ImplementedBy;
import app.com.horeich.services.exceptions.InvalidConfigurationException;
import app.com.horeich.services.runtime.IServicesConfig;
// import app.com.horeich.webservice.auth.IClientAuthConfig;

@ImplementedBy(Config.class)
public interface IConfig {

    /**
     * Get the TCP port number where the service listen for requests.
     *
     * @return TCP port number
     */
    int getPort() throws InvalidConfigurationException;

    /**
     * Service layer configuration
     */
    IServicesConfig getServicesConfig();

    /**
     * Client authorization configuration
     */
    // IClientAuthConfig getClientAuthConfig() throws InvalidConfigurationException;
}
