package org.gdl2.cdshooks;

import lombok.Value;
import org.gdl2.expression.ExpressionItem;

import java.util.List;

@Value
public class UseTemplate {
    private String templateId;
    private List<ExpressionItem> assignments;
}
