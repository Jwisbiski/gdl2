package org.gdl2.model;

import lombok.Value;
import org.gdl2.expression.ExpressionItem;

import java.util.List;
import java.util.Map;

@Value
public class DataBinding {
    private String id;
    private Type type;
    private String modelId;
    private String templateId;
    private Map<String, Element> elements;
    private List<ExpressionItem> predicates;

    public enum Type {
        INPUT, OUTPUT
    }
}