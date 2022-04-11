package app.com.horeich.webservice.lwm2m;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.node.LwM2mSingleResource;

import app.com.horeich.services.external.LwM2MValue;

public class PropertyModel {
    
    private Map<Integer, List<LwM2mResource>> mappedResources = new HashMap<Integer, List<LwM2mResource>>();



    public PropertyModel()
    {
        
    }

    public Map<Integer, List<LwM2mResource>> getObjectResources()
    {
        return mappedResources;
    }

    public void add(Integer objectId, LwM2mResource resource)
    {
        List<LwM2mResource> existingResources = mappedResources.get(objectId);
        if (existingResources == null)
        {
            List<LwM2mResource> newResources = new ArrayList<LwM2mResource>();
            newResources.add(resource);
            mappedResources.put(objectId, newResources);
        }
        else
        {
            existingResources.add(resource);
        }
    }

    public void add(String resourceName, LwM2MValue resourceValue, List<ObjectModel> objects)
    {
        for (ObjectModel object : objects)
        {
            for (ResourceModel resource : object.resources.values())
            {
                if (resource.name.equals(resourceName))
                {
                    LwM2mResource newResource = LwM2mSingleResource.newResource(resource.id, resourceValue.getValue());
                    List<LwM2mResource> resources = mappedResources.get(object.id);
                    if (resources == null)
                    {
                        List<LwM2mResource> newResources = new ArrayList<LwM2mResource>();
                        newResources.add(newResource);
                        mappedResources.put(object.id, newResources);
                    }
                    else
                    {
                        resources.add(newResource);
                    }
                }
            }
        }
    }
}
