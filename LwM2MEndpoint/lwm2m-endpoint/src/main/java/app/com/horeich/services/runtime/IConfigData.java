package app.com.horeich.services.runtime;

import app.com.horeich.services.exceptions.InvalidConfigurationException;

public interface IConfigData {
    String getString(String key);
    boolean getBool(String key);
    int getInt(String key) throws InvalidConfigurationException;
    boolean hasPath(String path);
}
