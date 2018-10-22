package org.gdl2.expression;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Collections;
import java.util.List;

import static org.gdl2.expression.OperatorKind.*;

/**
 * Any expression includes more than one pair of operands without any bracket.
 * <p></p>
 * Support operators:
 * Addition: +
 * Subtraction: -
 * Multiplication: *
 * Division: /
 * Remainder: %
 * Logic and: {@literal &&}
 * Logic or: ||
 * Exponent: ^
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class LongExpression extends ExpressionItem {
    private List<OperandPair> items;

    public LongExpression(List<OperandPair> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (OperandPair operandPair : this.items) {
            ExpressionItem expressionItem = operandPair.expressionItem;
            if (expressionItem instanceof LongExpression) {
                buf.append("(");
                buf.append(expressionItem.toString());
                buf.append(")");
            } else {
                buf.append(expressionItem.toString());
            }
            if (!operandPair.isLast()) {
                buf.append(operandPair.operator.getSymbol());
            }
        }
        return buf.toString();
    }

    public List<OperandPair> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Breaks down a long expression into chained binary expressions by operator precedence.
     *
     * @return binary expression
     */
    public BinaryExpression toBinaryExpression() {
        return nextBinaryExpressionByOperatorPrecedence(items);
    }

    private BinaryExpression nextBinaryExpressionByOperatorPrecedence(List<OperandPair> items) {
        int next = nextOperandPairByPrecedence(items);
        OperandPair operandPair = items.get(next);
        if (operandPair.isLast()) {
            return (BinaryExpression) operandPair.expressionItem;
        }
        OperatorKind op = operandPair.operator;
        ExpressionItem left;
        ExpressionItem right;
        BinaryExpression binaryExpression;
        left = operandPair.expressionItem;
        right = items.get(next + 1).expressionItem;
        binaryExpression = new BinaryExpression(left, right, op);
        OperandPair nextOperandPair = items.get(next + 1);
        items.remove(next);
        items.set(next, new OperandPair(binaryExpression, nextOperandPair.operator));
        return nextBinaryExpressionByOperatorPrecedence(items);
    }

    private int nextOperandPairByPrecedence(List<OperandPair> operandPairs) {
        for (int i = 0, j = operandPairs.size(); i < j; i++) {
            if (isHighestPrecedence(operandPairs.get(i).operator)) {
                return i;
            }
        }
        for (int i = 0, j = operandPairs.size(); i < j; i++) {
            if (isHigherPrecedence(operandPairs.get(i).operator)) {
                return i;
            }
        }
        return 0;
    }

    private boolean isHigherPrecedence(OperatorKind operator) {
        return operator == MULTIPLICATION || operator == DIVISION || operator == REMINDER;
    }

    private boolean isHighestPrecedence(OperatorKind operator) {
        return operator == EXPONENT;
    }

    @Value
    public static class OperandPair {
        private ExpressionItem expressionItem;
        private OperatorKind operator;

        public OperandPair(ExpressionItem expressionItem, OperatorKind operator) {
            this.expressionItem = expressionItem;
            this.operator = operator;
        }

        public OperandPair(ExpressionItem expressionItem) {
            this.expressionItem = expressionItem;
            this.operator = null;
        }

        public ExpressionItem getExpressionItem() {
            return expressionItem;
        }

        public OperatorKind getOperator() {
            return operator;
        }

        public boolean isLast() {
            return operator == null; // null if last pair
        }
    }
}