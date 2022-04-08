package app.com.horeich.services.lwm2m;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicModel implements LwM2mModel {
    
    private static final Logger LOG = LoggerFactory.getLogger(DynamicModel.class);

    private Map<Integer, ObjectModel> objects; // objects sorted by ID

    public DynamicModel(Collection<ObjectModel> objectModels) {
        objects = new HashMap<>();    
        for (ObjectModel model : objectModels) {
            ObjectModel old = objects.put(model.id, model);
            if (old != null) {
                LOG.debug("Model already exists for object {}. Overriding it.", model.id);
            }
        }

        if (objects.size() == 0)
        {
            LOG.debug("Dynamic model list is empty");
        }
    }

    public void add(ObjectModel objectModel)
    {
        ObjectModel old = objects.put(objectModel.id, objectModel);
        if (old != null)
        {
            LOG.debug("Model already exists for object {}. Overriding it.", objectModel.id);
        }
    }

    @Override
    public ResourceModel getResourceModel(int objectId, int resourceId) {
        ObjectModel object = objects.get(objectId);
        if (object != null) {
            return object.resources.get(resourceId);
        }
        return null;
    }

    @Override
    public ObjectModel getObjectModel(int objectId) {
        return objects.get(objectId);
    }

    @Override
    public Collection<ObjectModel> getObjectModels() {
        return objects.values();
    }
}
