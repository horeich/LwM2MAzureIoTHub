package com.example;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.client.resource.ResourceChangedListener;
import org.eclipse.leshan.client.servers.ServerIdentity;
import org.eclipse.leshan.core.Destroyable;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel.Type;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the following basic information:
 * - Manufacturer
 * - Model number
 * - Serial number
 * - Firmware version
 * - Battery level
 * - Current time
 * - UTC offset
 * Timezone
 * For more info see: OMA page 110
 */

public class DeviceObject extends BaseInstanceEnabler implements Destroyable {

    private static final List<Integer> supportedResources = Arrays.asList(0, 1, 2, 3, 4, 11, 13);

    private static final Logger LOG = LoggerFactory.getLogger(DeviceObject.class);

    private String manufacturer;
    private String modelNumber;
    private String serialNumber;
    private EnumSet<BindingMode> supportedBinding;

    private static final Random RANDOM = new Random();
            //19, 20, 21);

    private static final int CURRENT_TIME = 13;

    // private final Timer timer;

    public DeviceObject()
    {
        
    }

    public DeviceObject(String manufacturer, String modelNumber, String serialNumber) {
        this.manufacturer = manufacturer;
        this.modelNumber = modelNumber;
        this.serialNumber = serialNumber;
        this.supportedBinding = EnumSet.of(BindingMode.U);
        // notify new date each 5 second
        // this.timer = new Timer("Device-Current Time");
        // timer.schedule(new TimerTask() {
        //     @Override
        //     public void run() {
        //         fireResourcesChange(13);
        //     }
        // }, 5000, 5000);
    }

    @Override
    public ReadResponse read(ServerIdentity identity, int resourceid) {
        if (!identity.isSystem())
            LOG.info("Read on Device resource /{}/{}/{}", getModel().id, getId(), resourceid);
        switch (resourceid) {
        case 0:
            return ReadResponse.success(resourceid, getManufacturer());
        case 1:
            return ReadResponse.success(resourceid, getModelNumber());
        case 2:
            return ReadResponse.success(resourceid, getSerialNumber());
        case 3:
            return ReadResponse.success(resourceid, getFirmwareVersion());
        case 9:
            return ReadResponse.success(resourceid, getBatteryLevel());
        // case 10:
        //     return ReadResponse.success(resourceid, getMemoryFree());
        // case 11:
        //     Map<Integer, Long> errorCodes = new HashMap<>();
        //     errorCodes.put(0, getErrorCode());
        //     return ReadResponse.success(resourceid, errorCodes, Type.INTEGER);
        // case 13:
        //     return ReadResponse.success(resourceid, getCurrentTime());
        // case 14:
        //     return ReadResponse.success(resourceid, getUtcOffset());
        // case 15:
        //     return ReadResponse.success(resourceid, getTimezone());
        // case 16:
        //     return ReadResponse.success(resourceid, getSupportedBinding());
        // case 17:
        //     return ReadResponse.success(resourceid, getDeviceType());
        // case 18:
        //     return ReadResponse.success(resourceid, getHardwareVersion());
        // case 19:
        //     return ReadResponse.success(resourceid, getSoftwareVersion());
        // case 20:
        //     return ReadResponse.success(resourceid, getBatteryStatus());
        // case 21:
        //     return ReadResponse.success(resourceid, getMemoryTotal());
        default:
            return super.read(identity, resourceid);
        }
    }
    
    // @Override
    // public void notifyResourcesChanged(int... resourceIds) {
    //     for (ResourceChangedListener listener : listeners) {
    //         listener.resourcesChanged(resourceIds);
    //     }
    // }

    @Override
    public ObserveResponse observe(ServerIdentity identity) {
        // Perform a read by default
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

    @Override
    public ExecuteResponse execute(ServerIdentity identity, int resourceid, String params) {
        String withParams = null;
        if (params != null && params.length() != 0)
            withParams = " with params " + params;
        LOG.info("Execute on Device resource /{}/{}/{} {}", getModel().id, getId(), resourceid,
                withParams != null ? withParams : "");

        if (resourceid == 4) {
            new Timer("Reboot Lwm2mClient").schedule(new TimerTask() {
                @Override
                public void run() {
                    getLwM2mClient().stop(true);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                    getLwM2mClient().start();
                }
            }, 500);
        }
        return ExecuteResponse.success();
    }

    @Override
    public WriteResponse write(ServerIdentity identity, boolean replace, int resourceid, LwM2mResource value) {
        LOG.info("Write on Generic Device resource /{}/{}/{}", getModel().id, getId(), resourceid);

        switch (resourceid) {
        case CURRENT_TIME:
            LOG.info("Received current time {}", value.getValue());
            // Application.setTime();
            return WriteResponse.success();
        // case 14:
        //     setUtcOffset((String) value.getValue());
        //     fireResourcesChange(resourceid);
        //     return WriteResponse.success();
        // case 15:
        //     setTimezone((String) value.getValue());
        //     fireResourcesChange(resourceid);
        //     return WriteResponse.success();
        default:
            return super.write(identity, replace, resourceid, value);
        }
    }

    private String getManufacturer() {
        return "HOREICH device";
    }

    private String getModelNumber() { // IMSI
        return "1234567890";
    }

    private String getSerialNumber() {
        return "LOAD-L476RG";
    }

    private String getFirmwareVersion() {
        return "1.0.0";
    }

    private long getErrorCode() {
        return 0;
    }

    private int getBatteryLevel() {
        return RANDOM.nextInt(101);
    }

    private long getMemoryFree() {
        return Runtime.getRuntime().freeMemory() / 1024;
    }

    private Date getCurrentTime() {
        return new Date();
    }

    private String utcOffset = new SimpleDateFormat("X").format(Calendar.getInstance().getTime());

    private String getUtcOffset() {
        return utcOffset;
    }

    private void setUtcOffset(String t) {
        utcOffset = t;
    }

    private String timeZone = TimeZone.getDefault().getID();

    private String getTimezone() {
        return timeZone;
    }

    private void setTimezone(String t) {
        timeZone = t;
    }

    private String getSupportedBinding() {
        return "U";
    }

    private String getDeviceType() {
        return "Demo";
    }

    private String getHardwareVersion() {
        return "1.0.1";
    }

    private String getSoftwareVersion() {
        return "1.0.2";
    }

    private int getBatteryStatus() {
        return RANDOM.nextInt(7);
    }

    private long getMemoryTotal() {
        return Runtime.getRuntime().totalMemory() / 1024;
    }

    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }

    @Override
    public void destroy() {
        //timer.cancel();
    }
}
