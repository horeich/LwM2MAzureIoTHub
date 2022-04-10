package app.com.horeich.webservice.lwm2m;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.leshan.core.model.LwM2mModel;
import org.eclipse.leshan.core.model.LwM2mModelRepository;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.model.ResourceModel;
import org.eclipse.leshan.server.model.LwM2mModelProvider;
import org.eclipse.leshan.server.registration.Registration;

/**
 * A LwM2mModelProvider which supports object versioning. It returns a LwM2mModel taking into account object version
 * given in registration.
 */
public class DynamicVersionedModelProvider implements LwM2mModelProvider {

    private DynamicLwM2mModelRepository repository;

    public DynamicVersionedModelProvider(Collection<ObjectModel> objectModels) {
        this.repository = new DynamicLwM2mModelRepository(objectModels);
    }

    public DynamicVersionedModelProvider(DynamicLwM2mModelRepository repository) {
        this.repository = repository;
    }

    @Override
    public LwM2mModel getObjectModel(Registration registration) {
        return new DynamicModel(registration);
    }

    private class DynamicModel implements LwM2mModel {

        private final Registration registration;

        public DynamicModel(Registration registration) {
            this.registration = registration;
        }

        @Override
        public ResourceModel getResourceModel(int objectId, int resourceId) {
            ObjectModel objectModel = getObjectModel(objectId);
            if (objectModel != null)
                return objectModel.resources.get(resourceId);
            else
                return null;
        }

        @Override
        public ObjectModel getObjectModel(int objectId) {
            String version = registration.getSupportedVersion(objectId);
            if (version != null) {
                return repository.getObjectModel(objectId, version);
            }
            return null;
        }

        @Override
        public Collection<ObjectModel> getObjectModels() {
            Map<Integer, String> supportedObjects = registration.getSupportedObject();
            Collection<ObjectModel> result = new ArrayList<>(supportedObjects.size());
            for (Entry<Integer, String> supportedObject : supportedObjects.entrySet()) {
                ObjectModel objectModel = repository.getObjectModel(supportedObject.getKey(),
                        supportedObject.getValue());
                if (objectModel != null)
                    result.add(objectModel);
            }
            return result;
        }
    }
}