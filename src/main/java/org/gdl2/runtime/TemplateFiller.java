package org.gdl2.runtime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TemplateFiller {
    private static final Pattern VARIABLE_REGEX = Pattern.compile("\\{\\$gt([0-9.])+[0-9]?}");
    private static final String DOUBLE_NUM_PATTERN = "[\\x00-\\x20]*[+-]?(((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?"
            + "(\\p{Digit}+))?)|(\\.((\\p{Digit}+))([eE][+-]?(\\p{Digit}+))?)|(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?"
            + "(\\.)(\\p{XDigit}+)))[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*";
    private static final Pattern DOUBLE_NUM = Pattern.compile(DOUBLE_NUM_PATTERN);
    private static final Pattern INTEGER_NUM = Pattern.compile("^-?\\d+$");
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    String replaceVariablesWithValues(String source, Map<String, Object> localValues, Map<String, List<Object>> globalValues) {
        StringBuffer stringBuffer = new StringBuffer();
        Matcher matcher = VARIABLE_REGEX.matcher(source);

        while (matcher.find()) {
            String text = matcher.group();
            String key = text.substring(2, text.length() - 1);
            Object value = localValues.get(key);
            if (value == null && globalValues.containsKey(key)) {
                value = globalValues.get(key).get(0);
            }
            if (value != null) {
                String stringValue;
                if (value instanceof Date) {
                    stringValue = dateFormat.format(value);
                } else if (value instanceof LocalDateTime) {
                    stringValue = ((LocalDateTime) value).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } else {
                    stringValue = value.toString();
                }
                matcher.appendReplacement(stringBuffer, stringValue);
            }
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    void traverseMapAndReplaceAllVariablesWithValues(Map<String, Object> map, Map<String, Object> localValues,
                                                     Map<String, List<Object>> globalValues) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object object = entry.getValue();
            if (object instanceof String) {
                Object value = replaceAndCastToDoubleOrIntegerIfNeeded((String) object, localValues, globalValues);
                map.put(key, value);
            } else if (object instanceof Map) {
                traverseMapAndReplaceAllVariablesWithValues((Map) object, localValues, globalValues);
            } else if (object instanceof List) {
                traverseListAndReplaceAllVariablesWithValues((List) object, localValues, globalValues);
            }
        }
    }

    private void traverseListAndReplaceAllVariablesWithValues(List list, Map<String, Object> localValues,
                                                              Map<String, List<Object>> globalValues) {
        for (int i = 0, j = list.size(); i < j; i++) {
            Object object = list.get(i);
            if (object instanceof String) {
                Object value = replaceAndCastToDoubleOrIntegerIfNeeded((String) object, localValues, globalValues);
                list.set(i, value);
            } else if (object instanceof Map) {
                traverseMapAndReplaceAllVariablesWithValues((Map) object, localValues, globalValues);
            } else if (object instanceof List) {
                traverseListAndReplaceAllVariablesWithValues((List) object, localValues, globalValues);
            }
        }
    }

    private Object replaceAndCastToDoubleOrIntegerIfNeeded(String original, Map<String, Object> localValues,
                                                           Map<String, List<Object>> globalValues) {
        String replaced = replaceVariablesWithValues(original, localValues, globalValues);
        if (!replaced.equals(original)) {
            Object value = replaced;
            if (isInteger(replaced)) {
                value = Integer.parseInt(replaced);
            } else if (isDouble(replaced)) {
                value = Double.parseDouble(replaced);
            }
            return value;
        }
        return original;
    }

    static boolean isDouble(String value) {
        return DOUBLE_NUM.matcher(value).matches();
    }

    static boolean isInteger(String value) {
        return INTEGER_NUM.matcher(value).matches();
    }
}
