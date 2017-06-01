package org.gdl2.expression;

import java.util.Objects;

public class ReferenceVariable extends Variable {
    private int index;

    public ReferenceVariable(int index, String attribute) {
        super(null, null, null, attribute);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        ReferenceVariable that = (ReferenceVariable) obj;
        return index == that.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), index);
    }

    @Override
    public String toString() {
        return "ReferenceVariable{" + "index=" + index + '}';
    }
}
