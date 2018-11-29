package org.gdl2.expression;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class ExpressionItem {
    List<Variable> getVariables(ExpressionItem expressionItem) {
        Set<String> ids = new LinkedHashSet<>();
        getVariableIds(expressionItem, ids, false);
        List<Variable> variables = new ArrayList<>();
        for (String id : ids) {
            variables.add(Variable.createByCode(id));
        }
        return variables;
    }

    public Set<String> getVariableIdsExcludingNullValueChecks() {
        Set<String> ids = new LinkedHashSet<>();
        getVariableIds(this, ids, true);
        return ids;
    }

    private void getVariableIds(ExpressionItem expressionItem, Set<String> idList, boolean excludingNullValueChecks) {
        if (expressionItem instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expressionItem;
            if (excludingNullValueChecks && binaryExpression.getOperator().equals(OperatorKind.EQUALITY)
                    && binaryExpression.getRight().toString().equalsIgnoreCase("null")) {
                return; // skip e.g. "$gt0011==null"
            }
            getVariableIds(binaryExpression.getLeft(), idList, excludingNullValueChecks);
            getVariableIds(binaryExpression.getRight(), idList, excludingNullValueChecks);
        } else if (expressionItem instanceof LongExpression) {
            LongExpression longExpression = (LongExpression) expressionItem;
            BinaryExpression binaryExpression = longExpression.toBinaryExpression();
            getVariableIds(binaryExpression, idList, excludingNullValueChecks);
        } else if (expressionItem instanceof UnaryExpression) {
            UnaryExpression unaryExpression = (UnaryExpression) expressionItem;
            if (unaryExpression.getOperator().equals(OperatorKind.FIRED)
                    || unaryExpression.getOperator().equals(OperatorKind.NOT_FIRED)) {
                return; // skip e.g. "fired($gt0009)"
            }
            getVariableIds(unaryExpression.getOperand(), idList, excludingNullValueChecks);
        } else if (expressionItem instanceof Variable) {
            String id = ((Variable) expressionItem).getCode();
            if (!idList.contains(id)) {
                idList.add(id);
            }
        }
    }
}