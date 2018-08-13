package org.gdl2.model;

import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
public class Template {
    private String id;
    private String modelId;
    private String templateId;
    private Map<String, Object> object;
    private List<ElementBinding> elementBindings;
}