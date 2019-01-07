package org.gdl2.expression;

import lombok.Value;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Value
public class UseTemplateExpression extends ExpressionItem {
    private Variable variable;
    private List<AssignmentExpression> assignmentExpressions;
    private List<Variable> ifVariables;
    private Map<Variable, List<Variable>> inputVariableMap;

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("use_template(");
        buf.append(variable.toString());

        if (assignmentExpressions.size() > 0) {
            buf.append('(');
            Iterator<AssignmentExpression> iterator = assignmentExpressions.iterator();
            while (iterator.hasNext()) {
                AssignmentExpression assignmentExpression = iterator.next();
                buf.append(assignmentExpression.toString());
                if (iterator.hasNext()) {
                    buf.append(';');
                }
            }
            buf.append(")");
        }

        buf.append(')');
        return buf.toString();
    }
}
