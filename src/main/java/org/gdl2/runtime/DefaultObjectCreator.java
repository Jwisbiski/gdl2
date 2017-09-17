package org.gdl2.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

/**
 * The default object creator that creates simple objects based
 * on classes known to gdl2 library, e.g. built-in data types
 */
public class DefaultObjectCreator implements ObjectCreatorPlugin {
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").create();

    @Override
    public Object create(String modelId, Map<String, Object> values) throws ClassNotFoundException {
        String json = gson.toJson(values);
        Class modelClass = Class.forName(modelId);
        return gson.fromJson(json, modelClass);
    }
}
