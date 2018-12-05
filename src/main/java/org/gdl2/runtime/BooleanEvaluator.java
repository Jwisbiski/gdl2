package org.gdl2.runtime;

public class BooleanEvaluator {
    private static BooleanEvaluator booleanEvaluator = new BooleanEvaluator();

    private BooleanEvaluator() {
    }

    public static BooleanEvaluator getInstance() {
        return booleanEvaluator;
    }

    public Object logicAnd(Boolean value1, Boolean value2) {
        if (value1 == null) {
            return Boolean.FALSE.equals(value2) ? false : null;
        }
        if (value2 == null) {
            return Boolean.FALSE.equals(value1) ? false : null;
        }
        return value1 && value2;
    }

    public Object logicOr(Boolean value1, Boolean value2) {
        if (value1 == null) {
            return Boolean.TRUE.equals(value2) ? true : null;
        }
        if (value2 == null) {
            return Boolean.TRUE.equals(value1) ? true : null;
        }
        return value1 || value2;
    }

    public Object logicNot(Boolean value) {
        if (value == null) {
            return null;
        }
        return !value;
    }
}
