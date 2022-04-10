
package app.com.horeich.webservice.lwm2m;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.util.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of LWM2M object definitions which could contained several definitions of the same object in different
 * version.
 */
public class DynamicLwM2mModelRepository {
    private static final Logger LOG = LoggerFactory.getLogger(DynamicLwM2mModelRepository.class);

    // This map contains all the object models available. Different version could be used.
    // This map is indexed by a string composed of objectid and version (objectid##version)
    private Map<String, ObjectModel> objects;

    public DynamicLwM2mModelRepository(ObjectModel... objectModels) {
        this(Arrays.asList(objectModels));
    }

    public DynamicLwM2mModelRepository(Collection<ObjectModel> objectModels) {
        if (objectModels == null) {
            this.objects = new TreeMap<>();
        } else {
            Map<String, ObjectModel> map = new HashMap<>();

            for (ObjectModel model : objectModels) {
                String key = getKey(model); // (objectid##version)
                if (key == null) {
                    throw new IllegalArgumentException(
                            String.format("Model %s is invalid : object id is missing.", model));
                }
                ObjectModel old = map.put(key, model);
                if (old != null) {
                    LOG.debug("Model already exists for object {} in version {}. Overriding it.", model.id,
                            model.version);
                }
            }
            this.objects = map;
        }
    }

    public void addObjectModel(ObjectModel model)
    {
        String key = getKey(model); // (objectid##version)
        if (key == null) {
            throw new IllegalArgumentException(
                            String.format("Model %s is invalid : object id is missing.", model));
        }
        ObjectModel old = this.objects.put(key, model);
        if (old != null) {
            LOG.debug("Model already exists for object {} in version {}. Overriding it.", model.id,
                    model.version);
        }
    }

    public ObjectModel getObjectModel(Integer objectId, String version) {
        Validate.notNull(objectId, "objectid must not be null");
        Validate.notNull(version, "version must not be null");

        return objects.get(getKey(objectId, version));
    }

    private String getKey(ObjectModel objectModel) {
        return getKey(objectModel.id, objectModel.version);
    }

    private String getKey(Integer objectId, String version) {
        if (objectId == null) {
            return null;
        }
        return objectId + "##" + version;
    }
}
