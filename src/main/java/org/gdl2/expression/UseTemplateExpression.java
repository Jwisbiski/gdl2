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
            buf.append("(");
            Iterator<AssignmentExpression> iterator = assignmentExpressions.iterator();
            while (iterator.hasNext()) {
                AssignmentExpression assignmentExpression = iterator.next();
                buf.append(assignmentExpression.toString());
                if (iterator.hasNext()) {
                    buf.append(";");
                }
            }
            buf.append(")");
        }

        if (hasConditionalInputVariables()) {
            buf.append("[");
            if (ifVariables != null && ifVariables.size() > 0) {
                buf.append("if: ");
                Iterator<Variable> variableIterator = ifVariables.iterator();
                while (variableIterator.hasNext()) {
                    Variable variable = variableIterator.next();
                    buf.append(variable.toString());
                    if (variableIterator.hasNext()) {
                        buf.append(",");
                    }
                }
                buf.append("; ");
            }
            if (inputVariableMap != null) {
                Iterator<Map.Entry<Variable, List<Variable>>> inputKeyIterator = inputVariableMap.entrySet().iterator();
                while (inputKeyIterator.hasNext()) {
                    Map.Entry<Variable, List<Variable>> entry = inputKeyIterator.next();
                    Variable variable = entry.getKey();
                    Iterator<Variable> inputIterator = entry.getValue().iterator();
                    buf.append(variable.toString());
                    buf.append(": ");
                    while (inputIterator.hasNext()) {
                        Variable input = inputIterator.next();
                        buf.append(input.toString());
                        if (inputIterator.hasNext()) {
                            buf.append(",");
                        } else if (inputKeyIterator.hasNext()) {
                            buf.append("; ");
                        }
                    }
                }
            }
            buf.append("]");
        }

        buf.append(')');
        return buf.toString();
    }

    private boolean hasConditionalInputVariables() {
        return (ifVariables != null && ifVariables.size() > 0)
                || (inputVariableMap != null && inputVariableMap.size() > 0);
    }
}
