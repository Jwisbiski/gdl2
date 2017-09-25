package org.gdl2.expression;

import lombok.EqualsAndHashCode;
import lombok.Value;

import static java.lang.Integer.parseInt;

@EqualsAndHashCode(callSuper = true)
@Value
public class IntegerConstant extends ConstantExpression {
    private int integerValue;

    public IntegerConstant(String value) {
        super(value);
        if (value.startsWith("(")) {
            value = value.substring(1, value.length() - 1);
        }
        this.integerValue = parseInt(value);
    }

    public String toString() {
        return Integer.toString(integerValue);
    }
}
