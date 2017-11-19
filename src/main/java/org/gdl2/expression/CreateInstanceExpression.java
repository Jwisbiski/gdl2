package org.gdl2.expression;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Value
public final class CreateInstanceExpression extends AssignmentExpression {
    public static String FUNCTION_CREATE_NAME = "create";

    public CreateInstanceExpression(Variable variable, List<AssignmentExpression> assignmentExpressions) {
        super(variable, new MultipleAssignmentExpression(assignmentExpressions));
    }

    public List<AssignmentExpression> getAssignmentExpressions() {
        return ((MultipleAssignmentExpression) getAssignment()).getAssignmentExpressions();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getVariable());
        sb.append("(");
        List<AssignmentExpression> assignmentExpressions = getAssignmentExpressions();
        for (int i = 0, j = assignmentExpressions.size(); i < j; i++) {
            sb.append(assignmentExpressions.get(i).toString());
            if(i != j - 1) {
                sb.append(";");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}