package org.gdl2.runtime;

import org.gdl2.datatypes.DvDateTime;

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
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    Object replaceVariablesWithValues(String source, Map<String, Object> localValues, Map<String, List<Object>> globalValues) {
        if (isSingleVariable(source)) {
            return fetchValue(source, localValues, globalValues);
        }
        StringBuffer stringBuffer = new StringBuffer();
        Matcher matcher = VARIABLE_REGEX.matcher(source);
        while (matcher.find()) {
            Object value = fetchValue(matcher.group(), localValues, globalValues);
            matcher.appendReplacement(stringBuffer, value.toString());
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    private boolean isSingleVariable(String source) {
        return source.startsWith("{$") && source.endsWith("}");
    }

    private Object fetchValue(String variable, Map<String, Object> localValues, Map<String, List<Object>> globalValues) {
        String key = variable.substring(2, variable.length() - 1);
        Object value;
        if (key.endsWith(".all")) {
            key = key.substring(0, key.length() - 4);
            value = globalValues.get(key);
        } else {
            value = localValues.get(key);
            if (value == null && globalValues.containsKey(key)) {
                value = globalValues.get(key).get(0);
            }
        }
        if (value != null) {
            if (value instanceof Date) {
                value = dateFormat.format(value);
            } else if (value instanceof LocalDateTime) {
                value = ((LocalDateTime) value).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else if (value instanceof DvDateTime) {
                value = value.toString();
            }
        } else {
            value = variable;
        }
        return value;
    }

    void traverseMapAndReplaceAllVariablesWithValues(Map<String, Object> map, Map<String, Object> localValues,
                                                     Map<String, List<Object>> globalValues) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object object = entry.getValue();
            if (object instanceof String) {
                Object value = replaceVariablesWithValues((String) object, localValues, globalValues);
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
                Object value = replaceVariablesWithValues((String) object, localValues, globalValues);
                list.set(i, value);
            } else if (object instanceof Map) {
                traverseMapAndReplaceAllVariablesWithValues((Map) object, localValues, globalValues);
            } else if (object instanceof List) {
                traverseListAndReplaceAllVariablesWithValues((List) object, localValues, globalValues);
            }
        }
    }
}
