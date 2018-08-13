package org.gdl2.expression;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class ExpressionItem {
    List<Variable> getVariables(ExpressionItem expressionItem) {
        Set<String> ids = new LinkedHashSet<>();
        getVariableIds(expressionItem, ids);
        List<Variable> variables = new ArrayList<>();
        for (String id : ids) {
            variables.add(Variable.createByCode(id));
        }
        return variables;
    }

    private void getVariableIds(ExpressionItem expressionItem, Set<String> idList) {
        if (expressionItem instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expressionItem;
            getVariableIds(binaryExpression.getLeft(), idList);
            getVariableIds(binaryExpression.getRight(), idList);
        } else if (expressionItem instanceof UnaryExpression) {
            UnaryExpression unaryExpression = (UnaryExpression) expressionItem;
            getVariableIds(unaryExpression.getOperand(), idList);
        } else if (expressionItem instanceof Variable) {
            String id = ((Variable) expressionItem).getCode();
            if (!idList.contains(id)) {
                idList.add(id);
            }
        }
    }
}