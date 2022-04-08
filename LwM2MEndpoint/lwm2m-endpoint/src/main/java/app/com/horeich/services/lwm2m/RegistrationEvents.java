package app.com.horeich.services.lwm2m;

import org.eclipse.leshan.server.registration.*;
import org.joda.time.DateTime;

import akka.protobufv3.internal.UInt32Value;

import org.eclipse.leshan.server.californium.LeshanServer;
import org.eclipse.leshan.server.model.LwM2mModelProvider;

import app.com.horeich.services.exceptions.BaseException;
import app.com.horeich.services.exceptions.ResourceNotFoundException;
import app.com.horeich.services.external.AzureBridgeClient;
import app.com.horeich.services.external.LwM2MValue;
import app.com.horeich.services.external.PropertyApiModel;
import app.com.horeich.services.external.TwinApiModel;
import play.Logger;

import java.util.concurrent.*;

import javax.servlet.http.HttpServletRequest;

import com.typesafe.config.ConfigException.Null;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Time;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.StringUtils;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.leshan.core.*;
import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.model.ObjectLoader;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.core.model.ResourceModel.Type;
import org.eclipse.leshan.core.node.LwM2mMultipleResource;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mObject;
import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mPath;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.request.WriteRequest.Mode;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.core.request.*;

public class RegistrationEvents implements RegistrationListener {
    private static final Logger.ALogger LOG = Logger.of(RegistrationEvents.class);

    private static final String FORMAT_PARAM = "format";

    private final LeshanServer lwm2mServer;
    private AzureBridgeClient iotBridgeAdapter;
    private AzureBridgeLink bridgeLink;

    //private DynamicLwM2mModelRepository modelRepo = null;
     
    public RegistrationEvents(LeshanServer lwm2mServer, AzureBridgeLink bridgeLink) {
        this.lwm2mServer = lwm2mServer;
        this.bridgeLink = bridgeLink;
    }

    // @Override
    // public void registered(Registration registration, Registration previousReg,
    // Collection<Observation> previousObsersations) {
    // log.info("new device: " + registration.getEndpoint());

    // Link[] objectLinks = registration.getObjectLinks();

    // adapter.RegisterDeviceAsync("FAT2", "LoadSense");
    // }

    // private CompletionStage<ObserveResponse> observeAsync(LeshanServer server, )

    // private CompletionStage<WriteResponse> writeClient(LeshanServer server,
    // WriteRequest request, Registration registration, long timeout)
    // {
    // try {
    // WriteResponse response = server.send(registration, request, timeout);
    // return CompletableFuture.supplyAsync(() -> response);
    // }
    // catch (Exception e) {
    // // Error response
    // return CompletableFuture.supplyAsync(() -> new WriteResponse(code,
    // errorMessage));
    // }
    // }

    // private LwM2mNode extractLwM2mNode(String targetEndpoint, HttpServletRequest
    // req, LwM2mPath path) throws IOException {
    // String contentType = StringUtils.substringBefore(req.getContentType(), ";");
    // if ("application/json".equals(contentType)) {
    // String content = IOUtils.toString(req.getInputStream(),
    // req.getCharacterEncoding());
    // LwM2mNode node;
    // try {
    // node = gson.fromJson(content, LwM2mNode.class);
    // if (node instanceof LwM2mSingleResource) {
    // // TODO HACK resource type should be extracted from json value but this is
    // not yet available.
    // LwM2mSingleResource singleResource = (LwM2mSingleResource) node;
    // ResourceModel resourceModel = model.getResourceModel(path.getObjectId(),
    // singleResource.getId());
    // if (resourceModel != null) {
    // Type expectedType = resourceModel.type;
    // Object expectedValue = converter.convertValue(singleResource.getValue(),
    // singleResource.getType(), expectedType, path);
    // node = LwM2mSingleResource.newResource(node.getId(), expectedValue,
    // expectedType);
    // }
    // }
    // } catch (JsonSyntaxException e) {
    // throw new InvalidRequestException(e, "unable to parse json to tlv:%s",
    // e.getMessage());
    // }
    // return node;
    // } else if ("text/plain".equals(contentType)) {
    // String content = IOUtils.toString(req.getInputStream(),
    // req.getCharacterEncoding());
    // int rscId = Integer.valueOf(target.substring(target.lastIndexOf("/") + 1));
    // return LwM2mSingleResource.newStringResource(rscId, content);
    // }
    // throw new InvalidRequestException("content type %s not supported",
    // req.getContentType());
    // }

    

    // private void extractNode(LwM2mPath path) {
    //     // Returns the model of a single resource in an object
    //     ObjectModel model = modelRepo.getObjectModel(path.getObjectId(), "1.1");
    //     ResourceModel resourceModel = model.getResourceModel(path.getObjectId(), path.getResourceId());
    //     if (resourceModel != null) // if it exists
    //     {
    //         // resourceModel.
    //     }
    // }

    private ObjectModel loadObjectModel(LwM2mPath path) {
        ObjectModel objectModel = null; // modelRepo.getObjectModel(path.getObjectId(), "1.1");
        if (objectModel == null) // does not exist yet
        {
            String modelPath = String.valueOf(path.getObjectId());
            modelPath += ".xml";
            if (modelPath != null) {
                String[] modelPaths = new String[] { modelPath };
                // FileInputStream fileStream = null;
                // try {
                // fileStream = new FileInputStream("64000.xml");
                // }
                // catch (FileNotFoundException e) {
                // // log.info(message {}, e.);
                // }
                try {
                    List<ObjectModel> objectModels = ObjectLoader.loadDdfResources("/customModels/", modelPaths);
                    
                    // Add models to dynamic model list
                    for (ObjectModel model : objectModels) {
                        //this.model.addObjectModel(model);
                        //modelRepo.addObjectModel(model);

                    }
                    
                    objectModel = objectModels.get(0);
                    return objectModel;
                } catch (Exception e) {
                    LOG.info("message {}", e.getMessage());
                }
            }
        }
        return null;
        // modelListaddAll(ObjectLoader.loadDdfResources("/models/",
        // LwM2mDemoConstant.modelPaths));
    }








    @Override
    public void updated(RegistrationUpdate update, Registration updatedReg, Registration previousReg) {
        //String deviceId = update.getEndpoint();
        LOG.info("Device with ID {} and registration {} is registered", updatedReg.getEndpoint(), updatedReg.getId());
    }

    // This method is called asynchronously => the client is becoming notified that
    // registration was successful
    @Override
    public void registered(Registration registration, Registration previousReg,
            Collection<Observation> previousObservations) {
        
        String deviceId = registration.getEndpoint();
        LOG.info("Device with ID {} and registration {} is registered", deviceId, registration.getId());
        
        try {

            // Returns 2.01 on Created and 2.00 on Updated
            bridgeLink.registerDevice(registration.getEndpoint());
            
            // Get all the object instances that are made available by the client
            // -> Do an observe on all of them (except default)
            bridgeLink.writeProperties(registration);
            
            // TODO: what happens if observe fails -> deregistration?
            // Always do a new observe on registration
            bridgeLink.observeResources(registration);
            
            // catch registration failed exception -> false

        } catch(Exception e) {
            LOG.error("{}", e);
            // RegistrationService registrationService = lwm2mServer.getRegistrationService();
            // registration.getId();
        }

        // Link[] objectLinks = registration.getObjectLinks();
        // try {

        //     // TODO: extract device Id
        //     // TODO: transfer in TLV mode
        //     // TODO: Check for existing instances -> if not existing -> unregister
        //     //LOG.info("device Id %s", registration.getId());
            
        //     /**
        //      * Note: The registration sends either available objects or - if available @
        //      * registration time - object instances
        //      */

        //     // Map<Integer, String> objects = registration.getSupportedObject();
        //     // CompletionStage<TwinApiModel> result =
        //     // adapter.RegisterDeviceAsync(registration.getEndpoint());

        //     /**
        //      * Order:
        //      * 1.) First register device (and wait until finished)
        //      * 2.) if registered, get uplink device properties and send them down to the
        //      * device by write-request
        //      * 3.) if sucessful, get downlink device properties and send them to the server
        //      */
            
        //     // Register to IoT Bridge
        //     try {
        //         CompletionStage<Void> registrationStage = iotBridgeAdapter.registerDeviceAsync(deviceId);
        //         registrationStage.toCompletableFuture().get();

        //     } catch (ResourceNotFoundException e) {
        //         LOG.error("Could not register device");
        //         // TODO: deregister device from server to force renewed registration
        //     }

        //     // Get properties from IoT Bridge
        //     PropertyApiModel propertyApiModel = null;
        //     try {
        //         CompletionStage<PropertyApiModel> writeStage = iotBridgeAdapter.getPropertiesAsync(deviceId);
        //         propertyApiModel = writeStage.toCompletableFuture().get();
        //     } catch (ResourceNotFoundException e) {
        //         LOG.error("Could not fetch properties");
        //     }

            // Get all the object instances that are made available by the client
            // -> Do an observe on all of them (except default)
        //     Set<LwM2mPath> paths = registration.getAvailableInstances();

        //     List<ObjectModel> loadedObjects = new ArrayList<ObjectModel>();
        //     for (LwM2mPath path : paths) {
        //         // TODO: use isOmaObject();
        //         if (path.getObjectId() > 7) // skip default objects
        //         {
        //             loadedObjects.add(loadObjectModel(path)); // TODO: error handling // object model not found exception
        //         }
        //     }
            
        //     // -> now all models are loaded
        //     // List<ObjectModel> rwModel = new ArrayList<ObjectModel>(); [deprecated]
            
        //     // -> get all r/w resources and map to fetched properties
        //     Hashtable<String, LwM2MValue> fetchedProperties = propertyApiModel.getProperties();
        //     PropertyServiceModel serviceModel = new PropertyServiceModel();
        //     for (ObjectModel object : loadedObjects)
        //     {
        //         for (ResourceModel resource : object.resources.values())
        //         {
        //             if (resource.operations.isWritable())
        //             {
        //                 String resourceName = resource.name;
        //                 LwM2MValue value = fetchedProperties.get(resourceName);
        //                 if (value != null)
        //                 {
        //                     LwM2mResource propertyResource = LwM2mSingleResource.newResource(resource.id, value.getValue());
        //                     serviceModel.add(object.id, propertyResource);
        //                 }
        //             }
        //         }
        //     }
            
        //     Set<Entry<Integer, List<LwM2mResource>>> propertySet =  serviceModel.getObjectResources().entrySet();
        //     for (Entry<Integer, List<LwM2mResource>> singleResource : propertySet)
        //     {
        //         // WriteRequest propertyRequest = new WriteRequest(Mode.UPDATE, 64001, 0, LwM2mSingleResource.newIntegerResource(1, 89));
        //         // TODO: why does notly UPDATE work on Multiple and Single Instance objects
        //         WriteRequest propertyRequest = new WriteRequest(Mode.UPDATE, ContentFormat.TLV, singleResource.getKey(), 0, singleResource.getValue());
        //         WriteResponse propertyResponse = lwm2mServer.send(registration, propertyRequest);
        //     }
            

        //     Date currentDate = new Date();

        //     // Time is represented in ms in java but unix time uses a singed integer
        //     // (32bit/4byte)
        //     // int unixTime = (int)(currentDate.getTime() / 1000);

        //     // change values of multiple resources in object instance 3/0 (DEVICE)

        //     // Single resource: The resource with id = x can only have a single value
        //     // Multiple resource: The resource with id = x can have multiple values
        //     // (enumeration)

        //     // Time is represented as a 6 byte integer (see p.51)
        //     Time time = new Time(currentDate.getTime());
        //     LwM2mResource timeResource = LwM2mSingleResource.newDateResource(LwM2mResourceId.CURRENT_TIME, time);

        //     // Collection represents: Lists, Set, Queue, etc.
        //     ArrayList<LwM2mResource> resources = new ArrayList<LwM2mResource>();
        //     resources.add(timeResource);

        //     // LwM2mPath path = new LwM2mPath(LwM2mId.DEVICE, 0);
        //     // LwM2mObjectInstance deviceInstance = new LwM2mObjectInstance(0, resources);
            
        //     // @Note:
        //     WriteRequest deviceRequest = new WriteRequest(Mode.REPLACE, ContentFormat.TLV, LwM2mId.DEVICE, 0, resources);
        //     WriteResponse response = lwm2mServer.send(registration, deviceRequest, 3000);
        //     ResponseCode code = response.getCode();
            
        //     // ReadRequest readRequest = new ReadRequest(ContentFormat.TLV, 3);
        //     // ReadResponse re = lwm2mServer.send(registration, readRequest);

        //     // Observe all objects (Device, Telemetry, Property)

        //     for (LwM2mPath path : registration.getAvailableInstances()) {
        //         if (path.getObjectId() == 64000)
        //         {
        //             ObserveRequest request = new ObserveRequest(ContentFormat.TLV, path.getObjectId()); // only TLV or JSON supported!
        //             ObserveResponse observeResponse = lwm2mServer.send(registration, request, 2000000000);
        //             bridgeLink.forward(path.getObjectId(), registration.getEndpoint(), observeResponse.getContent());
                    
        //         }
                

        //         // Extract answer
        //         // Response should be handled on OnResponse in ObservationEvents
        //     }
        // } catch (Exception e) { // BaseException
        //     LOG.info("Exception {}" + e.getMessage());
        // }
    }

    /**
     * Method is called in both cases when a device actively unregisters from the
     * server and when it is unregistered by the server due to a timeout
     */
    @Override
    public void unregistered(Registration registration, Collection<Observation> observations, boolean expired, Registration newReg) {

        String deviceId = registration.getEndpoint();

        try {
            CompletionStage<Void> registrationStage = iotBridgeAdapter.unregisterDeviceAsync(deviceId);
            registrationStage.toCompletableFuture().get();

        } catch (Exception e) {
            LOG.error("Could not register device");
            // TODO: deregister device from server to force renewed registration
        }

        LOG.info("device deregistered %s: " + registration.getEndpoint());
    }

    // @Override
    // public void unregistered(Registration registration, Collection<Observation>
    // observations, boolean expired, Registration newReg) {
    // System.out.println("device left: " + registration.getEndpoint());
    // }
}


  // for (ObjectModel object : loadedObjects)
            // {
            //     for (ResourceModel resource : object.resources.values())
            //     {
            //         if (resource.operations.isWritable())
            //         {
            //             rwModel.add(object);
            //             break;
            //         }
            //     }
            // }

            // Map resource IDs to properties
            // Convert to service model

            // PropertyServiceModel serviceModel = new PropertyServiceModel();
            // Hashtable<String, LwM2MValue> properties = propertyApiModel.getProperties();
            // Set<Entry<String, LwM2MValue>> entrySet = properties.entrySet();

            // for (Entry<String, LwM2MValue> entry : entrySet) {
            //     serviceModel.add(entry.getKey(), entry.getValue(), rwModel);
            // }

            
            // if ()
            // {
            //     ContentFormat contentFormat = ContentFormat.TLV;
            //     if (serviceModel.getResources().size() > 0) {
            //         WriteRequest propertyRequest = new WriteRequest(Mode.UPDATE, 64001, 0,
            //                 serviceModel.getResources());
            //         WriteResponse propertyResponse = lwm2mServer.send(registration, propertyRequest);
            //     }     
            // }

                        //.thenAcceptAsync(propertyApiModel -> {
                           // try {


                                // if (propertyApiModel.getProperties().size() > 0)
                                // {

                                // Hashtable<String, LwM2MValue> properties = propertyApiModel.

                                // Set<String> keys = propertyApiModel.getProperties().keySet();

                                // for (String property : keys)
                                // {
                                // keys.
                                // }
                                // WriteRequest propertiesRequest = new WriteRequest(Mode.UPDATE, 64001, 0, )

                                // }
                                // ReadResponse response = lwm2mServer.send(registration, request, 4000);
                                // ResponseCode x = response.getCode();
            //                 } catch (Exception e) {
            //                     log.debug(String.format("Could not fetch properties %s", e.getMessage()));
            //                 }
            //             });

            //     ;

            //     writeStage.toCompletableFuture().get();
            //     // PropertyApiModel model = propertyStage.toCompletableFuture().get();

            //     // Hashtable<String, LwM2MValue> table = model.getProperties();
            // } catch (ResourceNotFoundException e) {
            //     log.debug("Could not fetch properties");
            // }


            // TODO: property object

            // ObserveRequest request = new ObserveRequest(contentFormat, "64000/0");
            // ObserveResponse cResponse = lwm2mServer.send(registration, request, 2000);
            // cResponse.toString();

            // // CompletionStage<Void> writeStage = iotBridgeAdapter.
            // // getPropertiesAsync(endpointId).
            // // thenAccept(propertyApiModel -> {
            // // // TODO: send downlink
            // // });

            // // writeStage.toCompletableFuture().get();

            // // Create write request
            // // ContentFormat contentFormat = ContentFormat.TLV; // use binary TLV format
            // // Mode mode = Mode.UPDATE;
            // // String target = "64000/0/5700";
            // // // alternativ: Lwm2mPath("64000/0");
            // // //LwM2mPath path = new LwM2mPath(64000, 0, 5700);
            // // LwM2mNode node = LwM2mSingleResource.newFloatResource(5700, 1.0);

            // CompletionStage<WriteResponse> response = writeClient(server, request,
            // registration, timeout).
            // thenAccept(writeResponse -> {

            // });

            // CompletableFuture.supplyAsync(server ->
            // {
            // server.send()
            // });

            // CompletionStage<Void> readStage = iotBridge

            // TwinApiModel model = result.toCompletableFuture().join();

            // TODO: if success -> start observe request, unregister otherwise

            // Start observe mode OECb3qhxrT
            // HttpServletRequest req = new HttpServletRequest();

            // String contentFormatParam = req.getParameter(FORMAT_PARAM);
            // ContentFormat contentFormat = contentFormatParam != null
            // ? ContentFormat.fromName(contentFormatParam.toUpperCase())
            // : null;
            // String target = "/";

            // NOTE: target or path are used interchangeably

            // WriteAttributesRequest writeRequest = new
            // WriteAttributesRequest("3303/0/5700", attributes)

            // Minimum observe request
            // ContentFormat contentFormat = new ContentFormat(11543); // Json
            // ObserveRequest request = new ObserveRequest(contentFormat, "64000/0");
            // ObserveResponse cResponse = lwm2mServer.send(registration, request, 2000);
            //cResponse.toString();

            /// ObserveResponse cResponse = lwm2mServer.send(registration, request, 6000);