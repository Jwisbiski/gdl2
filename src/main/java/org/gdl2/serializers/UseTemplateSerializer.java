package org.gdl2.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.gdl2.cdshooks.UseTemplate;

import java.lang.reflect.Type;
import java.util.stream.Collectors;

public class UseTemplateSerializer implements JsonSerializer<UseTemplate> {
    public JsonElement serialize(UseTemplate src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("template_id", src.getTemplateId());
        if (src.getAssignments() != null && !src.getAssignments().isEmpty()) {
            object.add("assignments", context.serialize(src.getAssignments().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList())));
        }
        return object;
    }
}