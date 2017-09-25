package org.gdl2.expression;

import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Value
public class DoubleConstant extends ConstantExpression {
    private double doubleValue;

    public DoubleConstant(String value) {
        super(value);
        if (value.startsWith("(")) {
            value = value.substring(1, value.length() - 1);
        }
        this.doubleValue = Double.valueOf(value);
    }

    public String toString() {
        return Double.toString(doubleValue);
    }
}
