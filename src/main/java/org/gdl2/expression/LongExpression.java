package org.gdl2.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Any expression includes more than one pair of operands without any bracket.
 * <p/>
 * Support operators:
 * Addition: +
 * Subtraction: -
 * Multiplication: *
 * Division: /
 * Remainder: %
 * Exponent: ^
 * Logic and: &&
 * Logic or: ||
 */
public class LongExpression extends ExpressionItem {
    private List<OperandPair> items;

    public LongExpression(List<OperandPair> items) {
        this.items = items;
    }

    public List<OperandPair> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Breaks down a long expression into binary expressions following operator precedence.
     *
     * @return list binary expressions
     */
    public List<BinaryExpression> toBooleanExpressions() {
        List<BinaryExpression> list = new ArrayList<>();

        return list;
    }

    public static class OperandPair {
        private ExpressionItem expressionItem;
        private OperatorKind operator;

        public OperandPair(ExpressionItem expressionItem, OperatorKind operator) {
            this.expressionItem = expressionItem;
            this.operator = operator;
        }

        public ExpressionItem getExpressionItem() {
            return expressionItem;
        }

        public OperatorKind getOperator() {
            return operator;
        }

        boolean isLast() {
            return operator == null; // null if last pair
        }
    }
}