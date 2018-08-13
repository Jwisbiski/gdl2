package org.gdl2.expression;

import lombok.Value;

import java.util.List;

@Value
public class AnyExpression extends ExpressionItem {
    private List<Variable> inputVariables;
    private ExpressionItem operand;

    public AnyExpression(List<Variable> inputVariables, ExpressionItem operand) {
        this.operand = operand;
        if (inputVariables == null || inputVariables.isEmpty()) {
            this.inputVariables = getVariables(operand);
        } else {
            this.inputVariables = inputVariables;
        }
    }
}
