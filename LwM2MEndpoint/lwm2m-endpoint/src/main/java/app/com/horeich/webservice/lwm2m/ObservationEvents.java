package app.com.horeich.webservice.lwm2m;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.core.model.StaticModel;
import org.eclipse.leshan.core.node.LwM2mMultipleResource;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mObject;
import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mResourceInstance;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.request.ContentFormat;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.demo.servlet.json.LwM2mNodeSerializer;
import org.eclipse.leshan.server.observation.ObservationListener;
import org.eclipse.leshan.server.registration.Registration;

import app.com.horeich.services.exceptions.ResourceNotFoundException;
import app.com.horeich.services.external.AzureBridgeClient;
import app.com.horeich.services.external.PropertyApiModel;
import app.com.horeich.services.external.TelemetryApiModel;
import app.com.horeich.services.external.ValueTypeListApiModel;
import app.com.horeich.webservice.iotbridge.AzureBridgeLink;
import jdk.internal.org.objectweb.asm.Type;
import play.Logger;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Dynamic;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;


public class ObservationEvents implements ObservationListener {

    private final Gson gson;
    private static final Logger.ALogger LOG = Logger.of(ObservationEvents.class);

    private final LeshanServer lwM2mServer;
    private AzureBridgeClient observationAdapter;
    private AzureBridgeLink bridgeLink;

    public ObservationEvents(LeshanServer lwM2mServer, AzureBridgeLink bridgeLink) {

        this.lwM2mServer = lwM2mServer;
        // this.observationAdapter = observationAdapter;
        this.bridgeLink = bridgeLink;

        GsonBuilder gsonBuilder = new GsonBuilder();
        // gsonBuilder.registerTypeHierarchyAdapter(LwM2mResponse.class, new ResponseSerializer());
        // gsonBuilder.registerTypeHierarchyAdapter(LwM2mNode.class, new LwM2mNodeSerializer());
        // gsonBuilder.registerTypeHierarchyAdapter(LwM2mNode.class, new LwM2mNodeDeserializer());
        //LwM2mNodeSerializer
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        this.gson = gsonBuilder.create();
    }

    @Override
    public void newObservation(Observation observation, Registration registration) {
        // TODO Auto-generated method stub
        LOG.info("New observation");
        // TODO: we can probably read data here too in the new observation message!
    }

    @Override
    public void cancelled(Observation observation) {
        // TODO Auto-generated method stub
        LOG.info("Obseration cancelled");

    }

    /**
     * Called when a notify is received from the client
     * What happens here?
     * 1) On Observation request the client will report on ALL its properties (for sync); forward them to the server
     * 2) On Notify the client will report on CHANGED properties; forward them to the server
     */
    @Override
    public void onResponse(Observation observation, Registration registration, ObserveResponse response) {
        
        LOG.info(observation.getPath().toString());
        String data = new StringBuilder("{\"ep\":\"").append(registration.getEndpoint()).append("\",\"res\":\"")
                        .append(observation.getPath().toString()).append("\",\"val\":")
                        .append(gson.toJson(response.getContent())).append("}").toString();
        LOG.info(data);
        
        /**
         * LwM2mResource can be LwM2mSingleResource or LwM2mMultipleResource
         * LwM2mResource extends LwM2mNode
         * LwM2mResourceInstance extends LwM2MNode
         */

        
        // Get the observed object ID (e.g. 64000)
        Integer objectId = observation.getPath().getObjectId(); //content.getId();

        bridgeLink.updateData(objectId, registration.getEndpoint(), response.getContent());

        // -> unpack object instance into resources with ids
        // TODO: Map vs. hashmap
        // TODO: no duplicate resource names allowed?

        // ObjectModel objectModel = this.modelRepository.getObjectModel(objectId, "1.1"); // must be loaded on registration
        // if (objectModel != null) { 
        //     TelemetryApiModel telemetryApiModel = new TelemetryApiModel();
        //     PropertyApiModel propertyApiModel = new PropertyApiModel();

        //     if (response.getContent() instanceof LwM2mSingleResource) {
        //         LwM2mResource resource = (LwM2mSingleResource)response.getContent();
        //         Integer resourceId = resource.getId();

        //         ResourceModel resourceModel = objectModel.resources.get(resourceId);//model.getResourceModel(objectId, resourceId);
        //         LOG.info(String.format("Received response: %,.2f", (double)resource.getValue()));

        //         if (resourceModel.operations.isWritable()) {
        //             propertyApiModel.setValue(resourceModel.name, resource);
        //         } else {
        //             telemetryApiModel.setValue(resourceModel.name, resource);
        //         }
        //     }
        //     else if (response.getContent() instanceof LwM2mResource)
        //     {
        //         LOG.info("content");
        //     }
        //     else if (response.getContent() instanceof LwM2mObject)
        //     {
        //         LwM2mObject instance = (LwM2mObject)response.getContent();

        //         // Collection<LwM2mResource> instances = content.getResources().values();
        //         // for (LwM2mResource resource : instances)
        //         // {
        //         //      log.info(String.format("Received notification for id %d: %,.2f", 
        //         //         item.getKey(), (double)item.getValue().getValue()));

        //         //     // log.info(String.format("Received notification for id %d: %,.2f", 
        //         //     //     item.getKey(), (double)item.getValue().getValue()));
        //         // }

        //         Map<Integer, LwM2mResource> instances = instance.getInstance(0).getResources();
        //         for (Map.Entry<Integer, LwM2mResource> singleResource : instances.entrySet()) {
        //             // log.info(String.format("Received notification for id %d: %,.2f", 
        //             //     singleResource.getKey(), (double)singleResource.getValue().getValue()));
        
        //             // Get LwM2mModel
        //             // int id = resource.getId();
        //             // extractResourceModel(observation.getPath(), resource);
                    
        //             // TODO: check for single instance

        //             LwM2mResource resource = singleResource.getValue();
        //             Integer resourceId = singleResource.getKey();

        //             ResourceModel resourceModel = objectModel.resources.get(resourceId);
        //             if (resourceModel.operations.isWritable()) {
        //                 propertyApiModel.setValue(resourceModel.name, resource);
        //             } else {
        //                 telemetryApiModel.setValue(resourceModel.name, resource);
        //             }
        //         }
        //     }
        //     else if (response.getContent() instanceof LwM2mObjectInstance) {
        //         LwM2mObjectInstance instance = (LwM2mObjectInstance)response.getContent();

        //         // Collection<LwM2mResource> instances = content.getResources().values();
        //         // for (LwM2mResource resource : instances)
        //         // {
        //         //      log.info(String.format("Received notification for id %d: %,.2f", 
        //         //         item.getKey(), (double)item.getValue().getValue()));

        //         //     // log.info(String.format("Received notification for id %d: %,.2f", 
        //         //     //     item.getKey(), (double)item.getValue().getValue()));
        //         // }

        //         Map<Integer, LwM2mResource> instances = instance.getResources();
        //         for (Map.Entry<Integer, LwM2mResource> singleResource : instances.entrySet()) {
        //             // log.info(String.format("Received notification for id %d: %,.2f", 
        //             //     singleResource.getKey(), (double)singleResource.getValue().getValue()));
        
        //             // Get LwM2mModel
        //             // int id = resource.getId();
        //             // extractResourceModel(observation.getPath(), resource);
                    
        //             // TODO: check for single instance

        //             LwM2mResource resource = singleResource.getValue();
        //             Integer resourceId = singleResource.getKey();
        //             ResourceModel resourceModel = objectModel.resources.get(resourceId);

        //             if (resourceModel.operations.isWritable()) {
        //                 propertyApiModel.setValue(resourceModel.name, resource);
        //             } else {
        //                 telemetryApiModel.setValue(resourceModel.name, resource);
        //             }
        //         }
        //         // CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(
        //         //     telemetryStage.toCompletableFuture(), propertyStage.toCompletableFuture());
        //         // combinedFuture.get();
        //         // device.toCompletableFuture().get();
        //     }
        //     if (propertyApiModel.getProperties().size() > 0) {
        //         try {
        //             CompletionStage<Void> propertyStage = observationAdapter.
        //                     updatePropertiesAsync(registration.getEndpoint(), propertyApiModel);
        //             propertyStage.toCompletableFuture().get();
        //         } catch (Exception e) {
        //             LOG.debug("Could not forward properties");
        //         }
        //     }
        //     else if (telemetryApiModel.getTelemetry().size() > 0)
        //     {
        //         try {
        //             CompletionStage<Void> telemetryStage = observationAdapter.
        //                     sendTelemetryAsync(registration.getEndpoint(), telemetryApiModel);
        //             telemetryStage.toCompletableFuture().get();
        //         } catch (Exception e) {
        //             LOG.debug("Could not forward telemetry");
        //         }
        //     }
        //}
    }

    // Called if an exception is thrown during dispatching (e.g. in onResponse)
    @Override
    public void onError(Observation observation, Registration registration, Exception error) {
        // TODO Auto-generated method stub
        LOG.info("Error in observation"); 
        // TODO:

    }

    private ResourceModel extractResourceModel(LwM2mPath objectPath, LwM2mResource lwm2mNode)
    {
        // ResourceModel resourceModel = model.getResourceModel(objectPath.getObjectId(), lwm2mNode.getId());

        // if (resourceModel != null)
        // {
        //     return resourceModel;
        // }
        // else
        // {
        //     // TODO: try load model?
        // }



        return null;
    }

}




            
            // // CompletionStage<Void> propertyStage = observationAdapter.getPropertiesAsync(registration.getEndpoint()).
            // // thenAcceptAsync(properties -> {

            // //     ContentFormat contentFormat = new ContentFormat(11543);
                
            // //     // Object value = 3.0;
            // //     // LwM2mNode node = LwM2mSingleResource.newResource(node.getId(), value, ResourceModel.Type.FLOAT);

            // //     // WriteRequest request = new WriteRequest(
            // //     //     WriteRequest.Mode.REPLACE,
            // //     //     contentFormat, 
            // //     //     "/",
            // //     //     node);
                
            // //     // try {
            // //     //     WriteResponse cResponse = lwM2MServer.send(registration, request, 2000); //  extractTimeout(req));

            // //     // } catch (Exception e) {
            // //     //     log.info(String.format("Exception: %s" + e.getMessage()));
            // //     // }
            // //     //processDeviceResponse(req, resp, cResponse);
            // // });

            //     // Object[] keyValue = new Object[] {1,"One"};

            //     // Pair<Integer, String> pair = new Pair<>(1, "One");
            //     // Pair<Object, String> pair = new Pair<>(resource.getValue(), resource.getType());
            //     // pair.set
                
            //     // TODO: check correct data type and convert... magicconverter....
            //     telemetry.put(resourceModel.name, new Object[] {resource.getValue(), resource.getType()});
          
