package app.com.horeich.webservice.iotbridge;

import java.time.Instant;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletionStage;

import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.core.model.ResourceModel.Operations;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mObject;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.request.ContentFormat;
import org.eclipse.leshan.core.request.ObserveRequest;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.request.WriteRequest.Mode;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.registration.Registration;

import app.com.horeich.services.exceptions.ResourceNotFoundException;
import app.com.horeich.services.external.AzureBridgeClient;
import app.com.horeich.services.external.LwM2MValue;
import app.com.horeich.services.external.PropertyApiModel;
import app.com.horeich.services.external.TelemetryApiModel;
import app.com.horeich.webservice.lwm2m.DynamicLwM2mModelRepository;
import app.com.horeich.webservice.lwm2m.LwM2mId;
import app.com.horeich.webservice.lwm2m.LwM2mResourceId;
import app.com.horeich.webservice.lwm2m.PropertyModel;
import play.Logger;

public class AzureBridgeLink {

    private static final Logger.ALogger LOG = Logger.of(AzureBridgeLink.class);

    private final LeshanServer lwm2mServer;
    private DynamicLwM2mModelRepository modelRepository;
    private AzureBridgeClient bridgeClient;

    public AzureBridgeLink(LeshanServer lwm2mServer, AzureBridgeClient bridgeClient,
            DynamicLwM2mModelRepository modelRepository) {
        this.lwm2mServer = lwm2mServer;
        this.modelRepository = modelRepository;
        this.bridgeClient = bridgeClient;
    }

    /**
     * Dynamically load Object Models from customModels folder
     * @param paths
     */
    private void loadObjectModels(Set<LwM2mPath> paths) {
        
        for (LwM2mPath path : paths) {
            // TODO: use isOmaObject();
            
            if (path.getObjectId() > 7 && path.isObjectInstance()) // skip default objects
            {
                //
                ObjectModel objectModel = modelRepository.getObjectModel(path.getObjectId(), "1.1");
                if (objectModel == null) { // does not exist yet
                    String modelPath = String.valueOf(path.getObjectId());
                    modelPath += ".xml";
                    if (modelPath != null) {
                        String[] modelPaths = new String[] { modelPath };
                        try {
                            List<ObjectModel> objectModels = ObjectLoader.loadDdfResources("/customModels/",
                                    modelPaths);
                            modelRepository.addObjectModel(objectModels.get(0));
                        } catch (Exception e) {
                            LOG.info("message {}", e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * Register device to Azure IoT Bridge.
     * Registration will automatically connect the device to the IoT Hub and it will be tagged CONNECTED
     * @param endpointId  Device ID in IoT Hub is the Endpoint ID in LwM2M
     */
    public void registerDevice(String endpointId) {
        try {
            CompletionStage<Void> registrationStage = bridgeClient.registerDeviceAsync(endpointId);
            registrationStage.toCompletableFuture().get();

        } catch (Exception e) {
            LOG.error("Could not register device");
            // TODO: deregister device from server to force renewed registration
        }
    }

    public void observeResources(Registration registration) {
        try {
            for (LwM2mPath path : registration.getAvailableInstances()) {
                // TODO: observe < 7 only specific resources
                if (path.getObjectId() > 7) {
                    ObserveRequest request = new ObserveRequest(ContentFormat.TLV, path.getObjectId()); // only TLV or
                                                                                                        // JSON
                    // supported!
                    ObserveResponse observeResponse = lwm2mServer.send(registration, request, 2000000000);

                    // The observe response already contains data ("notification")
                    updateData(path.getObjectId(), registration.getEndpoint(), observeResponse.getContent());
                }

                // bridgeClient.updatePropertiesAsync(endpointId, content)
                // Extract answer
                // Response should be handled on OnResponse in ObservationEvents
            }
        } catch (Exception e) {
            LOG.error("Error {0}", e);
        }
    }

    /**
     * Synchronize the reported properties
     * @param registration
     */
    public void syncProperties(Registration registration) {

        // Get reported properties from Azure IoT Bridge virtual device twin
        PropertyApiModel propertyServiceModel = null;
        try {
            CompletionStage<PropertyApiModel> writeStage = bridgeClient.getPropertiesAsync(registration.getEndpoint());
            propertyServiceModel = writeStage.toCompletableFuture().get();
        } catch (Exception e) {
            LOG.error("Could not fetch properties {0}", e);
        }

        // Get all the object instances that are made available by the client
        // -> Do an observe on all of them (except default)
        // A path is just the number representation e.g. 3/1/2 (object, object instance, resource)
        Set<LwM2mPath> registrationPaths = registration.getAvailableInstances();

        // Try load new object models (if not loaded yet)
        try {
            loadObjectModels(registrationPaths);
        } catch (Exception e) {
            LOG.error("Error loading models");
        }
        
        PropertyModel serviceModel = new PropertyModel();

        // Map all properties to objects and resources
        for (LwM2mPath path : registrationPaths) {
            if (path.isObjectInstance()) // e.g. /64000/0
            {
                // Load the object model according to the object id
                // and go through all resources of the model and add to downlink property if NAMES match
                ObjectModel objectModel = modelRepository.getObjectModel(path.getObjectId(), "1.1");
                for (ResourceModel resource : objectModel.resources.values()) {
                    
                    // Get the value of the upstream property by its name derived from object model
                    LwM2MValue value = propertyServiceModel.getValue(resource.name); // return null if name does not exist
                    if (value != null && resource.operations == Operations.RW) { // check is property (RW)
                        serviceModel.add(path.getObjectId(), LwM2mSingleResource.newResource(resource.id, value.getValue()));
                    }
                }
            } else {
                LOG.error("Invalid path format");
            }
        }

         // Set current time
        Instant now = Instant.now();
        serviceModel.add(
            LwM2mId.DEVICE, LwM2mSingleResource.newDateResource(LwM2mResourceId.CURRENT_TIME, Date.from(now))); 

        // We have now compiled a list of objects and resources with the property RW
        // Send them now downlink to the model

        try {
            // Perform all necessary write requests to update
            Set<Entry<Integer, List<LwM2mResource>>> propertySet = serviceModel.getObjectResources().entrySet();
            for (Entry<Integer, List<LwM2mResource>> singleResource : propertySet) {
                // WriteRequest propertyRequest = new WriteRequest(Mode.UPDATE, 64001, 0,
                // LwM2mSingleResource.newIntegerResource(1, 89));
                // TODO: why does notify UPDATE work on Multiple and Single Instance objects
                // TODO: Add instance to properties
                int objectId = singleResource.getKey();
                List<LwM2mResource> resources = singleResource.getValue();
                WriteRequest propertyRequest = new WriteRequest(
                    Mode.UPDATE, 
                    ContentFormat.TLV, 
                    objectId,
                    0, 
                    resources);
                WriteResponse propertyResponse = lwm2mServer.send(registration, propertyRequest);
                if (propertyResponse.isSuccess())
                {
                    LOG.info("Successfully wrote {0} to client", objectId);
                }
            }
        } catch (Exception e) {
            LOG.error("Error {0}", e);
        }
    }

    /**
     * 
     * @param objectId
     * @param endpointId
     * @param node
     */
    public void updateData(Integer objectId, String endpointId, LwM2mNode node) {
        ObjectModel objectModel = this.modelRepository.getObjectModel(objectId, "1.1");
        if (objectModel != null) {
            TelemetryApiModel telemetryApiModel = new TelemetryApiModel();
            PropertyApiModel propertyApiModel = new PropertyApiModel();
            
            // TODO: error Cannot read field "operations" because "resourceModel" is null
            // This error happens when client model and server model id do not match
            // (Nullptr exception)

            // Serialize in API model
            if (node instanceof LwM2mObject) {
                LwM2mObject lwm2mObject = (LwM2mObject) node;
                Map<Integer, LwM2mResource> resources = lwm2mObject.getInstance(0).getResources();

                for (Map.Entry<Integer, LwM2mResource> item : resources.entrySet()) {
                    // TODO: multiple resources
                    LwM2mResource resource = item.getValue(); // can be single or multiple resource
                    Integer resourceId = item.getKey();

                    // ObjectModel contains resource model with id
                    ResourceModel resourceModel = objectModel.resources.get(resourceId);
                    if (resourceModel.operations.isWritable()) {
                        propertyApiModel.setValue(resourceModel.name, resource);
                    } else {
                        telemetryApiModel.add(resourceModel.name, resource);
                    }
                }
            }

            // Send to bridge
            // Telemetry is sent as telemetry
            // Properties are sent as properties
            if (propertyApiModel.getProperties().size() > 0) {
                try {
                    CompletionStage<Void> propertyStage = bridgeClient
                            .updatePropertiesAsync(endpointId, propertyApiModel);
                    propertyStage.toCompletableFuture().get();
                } catch (Exception e) {
                    LOG.debug("Could not forward properties");
                }
            }
            if (telemetryApiModel.getTelemetry().size() > 0) {
                try {
                    CompletionStage<Void> telemetryStage = bridgeClient
                            .sendTelemetryAsync(endpointId, telemetryApiModel);
                    telemetryStage.toCompletableFuture().get();
                } catch (Exception e) {
                    LOG.debug("Could not forward telemetry");
                }
            }
        }
    }
}
