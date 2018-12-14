package org.gdl2.expression;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.ArrayList;
import java.util.Arrays;
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
 * Equal: ==
 * Not equal: !=
 * Greater than: &lt;
 * Less than: &lt;
 * Greater than or equal: &gt;=
 * Less than or equal: &lt;=
 * <p></p>
 * The precedence levels of operators are as https://en.wikipedia.org/wiki/Order_of_operations#Programming_languages
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class LongExpression extends ExpressionItem {
    private static final OperatorKind[][] OPERATORS_PRECEDENCE_LEVELS = {
            {EXPONENT},
            {MULTIPLICATION, DIVISION, REMINDER},
            {ADDITION, SUBTRACTION},
            {LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL},
            {EQUALITY, UNEQUAL},
            {AND},
            {OR}
    };

    private final List<OperandPair> items;

    public LongExpression(List<OperandPair> items) {
        this.items = Collections.unmodifiableList(items);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (OperandPair operandPair : this.items) {
            ExpressionItem expressionItem = operandPair.expressionItem;
            if (expressionItem instanceof LongExpression || expressionItem instanceof BinaryExpression) {
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
        List<OperandPair> operandPairs = new ArrayList<>(items);
        return nextBinaryExpressionByOperatorPrecedence(operandPairs);
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
        if (operandPair.expressionItem instanceof LongExpression) {
            left = ((LongExpression) operandPair.expressionItem).toBinaryExpression();
        } else {
            left = operandPair.expressionItem;
        }
        if (items.get(next + 1).expressionItem instanceof LongExpression) {
            right = ((LongExpression) items.get(next + 1).expressionItem).toBinaryExpression();

        } else {
            right = items.get(next + 1).expressionItem;
        }
        binaryExpression = new BinaryExpression(left, right, op);
        OperandPair nextOperandPair = items.get(next + 1);
        items.remove(next);
        items.set(next, new OperandPair(binaryExpression, nextOperandPair.operator));
        return nextBinaryExpressionByOperatorPrecedence(items);
    }

    private int nextOperandPairByPrecedence(List<OperandPair> operandPairs) {
        for (OperatorKind[] operatorKinds : OPERATORS_PRECEDENCE_LEVELS) {
            for (int i = 0, j = operandPairs.size(); i < j; i++) {
                OperatorKind operatorKind = operandPairs.get(i).operator;
                if (operatorKind != null && Arrays.stream(operatorKinds).anyMatch(operandPairs.get(i).operator::equals)) {
                    return i;
                }
            }
        }
        return 0;
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