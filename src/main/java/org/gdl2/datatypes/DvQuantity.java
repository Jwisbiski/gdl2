package org.gdl2.datatypes;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

@Value
@Builder
@NoArgsConstructor(force = true)
public final class DvQuantity {
    private static final char DECIMAL_SEPARATOR = '.';
    private String unit;
    private double magnitude;
    private int precision;

    public DvQuantity(String unit, double magnitude, int precision) {
        this.magnitude = magnitude;
        this.precision = precision;
        this.unit = unit;
    }

    public DvQuantity(double magnitude) {
        this("", magnitude, 0);
    }

    public static DvQuantity valueOf(String value) {
        int index = value.indexOf(",");
        String num = value;
        String units = "";

        if (index >= 0) {
            num = value.substring(0, index);
            units = value.substring(index + 1);
        }
        int precision = 0;
        index = num.indexOf(DECIMAL_SEPARATOR);
        if (index >= 0) {
            precision = num.length() - index - 1;
        }
        try {
            double magnitude = Double.parseDouble(num);
            return new DvQuantity(units, magnitude, precision);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("failed to parse quantity[" + num + "]", nfe);
        }
    }

    @Override
    public String toString() {
        DecimalFormat format = new DecimalFormat();
        format.setMinimumFractionDigits(precision);
        format.setMaximumFractionDigits(precision);
        DecimalFormatSymbols dfs = format.getDecimalFormatSymbols();
        dfs.setDecimalSeparator(DECIMAL_SEPARATOR);
        format.setDecimalFormatSymbols(dfs);
        format.setGroupingUsed(false);
        return format.format(magnitude) + ((unit == null || unit.isEmpty()) ? "" : "," + getUnit());
    }

    public Boolean equalityCheckWithMagnitudeUnit(Object object) {
        if (!(object instanceof DvQuantity)) {
            return false;
        }
        DvQuantity dvQuantity = (DvQuantity) object;
        if (dvQuantity.magnitude != this.magnitude) {
            return false;
        }
        if (dvQuantity.unit != null) {
            return dvQuantity.unit.equals(this.unit);
        }
        return this.unit == null;
    }
}