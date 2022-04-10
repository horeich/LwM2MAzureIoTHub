package app.com.horeich;

import app.com.horeich.services.helpers.*;
import app.com.horeich.services.http.*;
import app.com.horeich.services.runtime.*;
import scala.concurrent.Await;

import app.com.horeich.services.external.*;
import app.com.horeich.webservice.iotbridge.AzureBridgeLink;
import app.com.horeich.webservice.lwm2m.*;
import app.com.horeich.webservice.runtime.*;

import play.Logger;

import org.eclipse.leshan.*;
import org.eclipse.leshan.server.californium.*;
import org.eclipse.leshan.server.model.LwM2mModelProvider;
import org.eclipse.leshan.server.model.VersionedModelProvider;
import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.StaticModel;
import org.eclipse.leshan.core.observation.*;
import org.eclipse.leshan.server.registration.*;

import java.util.*;

public class LwM2MBackend {
    private static final Logger.ALogger log = Logger.of(LwM2MBackend.class);

    public static void main(String[] args) // TODO: make static
    {
        System.out.println("Hello World!");

        IHttpClient httpClient = new HttpClient();

        HttpClientWrapper httpClientWrapper = new HttpClientWrapper(httpClient);

        // IServicesConfig config = new ServiceConfig();

        // Loads config data from application.conf file
        Config config = new Config();

        System.out.println(config.getServicesConfig().getTelemetryUrl());

        // TelemetryApiModel telemetry = new TelemetryApiModel();
        // TelemetryAdapter telemetryAdapter = new TelemetryAdapter(httpClientWrapper, config.getServicesConfig());

        // telemetry.setHumidity("40");
        // telemetry.setTemperature("25");

        LeshanServerBuilder builder = new LeshanServerBuilder();
        // DynamicModel objectModel = null;
        // TODO: add coap config
        try {

            // List<ObjectModel> modelList = ObjectLoader.loadAllDefault();

            // Model path is "target/resources/models"
            // String[] modelPaths = new String[] { "64000.xml", "64001.xml" };

            List<ObjectModel> objectModels = ObjectLoader.loadAllDefault();
            // modelList.addAll(ObjectLoader.loadDdfResources("/customModels/",
            // modelPaths));
            // // if (modelsFolderPath != null) {
            // // models.addAll(ObjectLoader.loadObjectsFromDir(new File(modelsFolderPath),
            // true));
            // // }
            // Set provider for server, otherwise messages cannot be automatically decoded
            // and are shown as OPAQUE

            // objectModel = new DynamicModel(objectModels);

            // Collection of LwM2M model definitions (e.g. from xml files)
            DynamicLwM2mModelRepository modelRepository = new DynamicLwM2mModelRepository(objectModels);

            // To decode/encode messages from clients we need a model provider
            LwM2mModelProvider modelProvider = new DynamicVersionedModelProvider(modelRepository);
            builder.setObjectModelProvider(modelProvider);

            LeshanServer lwm2mServer = builder.build();

            AzureBridgeClient ioTBridgeAdapter = new AzureBridgeClient(httpClient, config.getServicesConfig());
            AzureBridgeLink bridgeLink = new AzureBridgeLink(lwm2mServer, ioTBridgeAdapter, modelRepository);

            // Listener to call events on register/unregister/update
            RegistrationEvents registrationListener = new RegistrationEvents(lwm2mServer, bridgeLink);
            // TelemetryAdapter telemetryAdapter = new TelemetryAdapter(httpClient,
            // config.getServicesConfig());
            
            // TODO: test multiple and single instance object models

            // Listener to call events on notify
            ObservationEvents observationListener = new ObservationEvents(lwm2mServer, bridgeLink);

            lwm2mServer.getRegistrationService().addListener(registrationListener);
            lwm2mServer.getObservationService().addListener(observationListener);

            lwm2mServer.start();
        } catch (Exception exception) {
            log.error("Error loading models");
        }

        // server.getRegistrationService().addListener(new RegistrationListener() {

        // @Override
        // public void registered(Registration registration, Registration previousReg,
        // Collection<Observation> previousObsersations) {
        // onRegistration(registration, previousReg, previousObsersations);
        // System.out.println("new device: " + registration.getEndpoint());
        // }

        // @Override
        // public void updated(RegistrationUpdate update, Registration updatedReg,
        // Registration previousReg) {
        // System.out.println("device is still here: " + updatedReg.getEndpoint());
        // }

        // @Override
        // public void unregistered(Registration registration, Collection<Observation>
        // observations, boolean expired,
        // Registration newReg) {
        // System.out.println("device left: " + registration.getEndpoint());
        // }
        // });

        try {
            // telemetryAdapter.UpdateTelemetryAsync(telemetry);

        } catch (Exception e) {

        }

        // try {
        // telemetryAdapter.UpdateTelemetryAsync(telemetry, )
        // } catch (Exception e) {
        // //String errorMessage = String.format("Failed to seed default rule %s",
        // rule.getDescription());
        // this.log.error("message here", e);
        // throw new ExternalDependencyException(errorMessage, e);
        // }

    }

}
