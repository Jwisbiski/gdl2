package org.gdl2.expression;

import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
public class UseTemplateExpression extends ExpressionItem {
    private Variable variable;
    private List<AssignmentExpression> assignmentExpressions;
    private List<Variable> ifVariables;
    private Map<Variable, List<Variable>> inputVariableMap;
}
