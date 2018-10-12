package org.gdl2.runtime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TemplateFiller {
    private static final Pattern VARIABLE_REGEX = Pattern.compile("\\{\\$gt([0-9.])+[0-9]?}");
    private static final String ALL = ".all";
    private static final String ALL_ENDING = ALL + "}";
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    Object replaceVariablesWithValues(String source, Map<String, Object> localValues,
                                      Map<String, List<Object>> globalValues) {
        return replaceVariablesWithValues(source, localValues, globalValues, null);
    }

    Object replaceVariablesWithValues(String source, Map<String, Object> localValues,
                                      Map<String, List<Object>> globalValues, Map<String, Object> additionalInputValues) {
        if (isSingleVariable(source)) {
            return fetchValue(source, localValues, globalValues, additionalInputValues);
        }
        StringBuffer stringBuffer = new StringBuffer();
        Matcher matcher = VARIABLE_REGEX.matcher(source);
        while (matcher.find()) {
            Object value = fetchValue(matcher.group(), localValues, globalValues, additionalInputValues);
            if (value != null) {
                matcher.appendReplacement(stringBuffer, value.toString());
            }
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    private boolean isSingleVariable(String source) {
        return source.startsWith("{$")
                && source.endsWith("}")
                && source.indexOf("}") == source.length() - 1;
    }

    private Object fetchValue(String variable, Map<String, Object> localValues, Map<String, List<Object>> globalValues,
                              Map<String, Object> additionalInputValues) {
        String key = variable.substring(2, variable.length() - 1);
        Object value;
        if (key.endsWith(ALL)) {
            key = key.substring(0, key.length() - 4);
            value = globalValues.get(key);
        } else {
            value = localValues.get(key);
            if (value instanceof List) {
                List list = (List) value;
                if (list.size() > 0) {
                    value = list.get(list.size() - 1);
                }
            }
            if (value == null && globalValues.containsKey(key)) {
                List<Object> valueList = globalValues.get(key);
                if (!valueList.isEmpty()) {
                    value = valueList.get(valueList.size() - 1);
                }
            }
        }
        if (value != null) {
            if (value instanceof Date) {
                value = dateFormat.format(value);
            } else if (value instanceof LocalDateTime) {
                value = ((LocalDateTime) value).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else if (value instanceof ZonedDateTime) {
                value = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format((ZonedDateTime) value);
            }
        } else if (additionalInputValues != null && additionalInputValues.containsKey(variableCode(variable))) {
            String variableCode = variableCode(variable);
            value = additionalInputValues.get(variableCode);
        }
        return value;
    }

    private String variableCode(String variable) {
        return variable.substring(2, variable.length() - 1);
    }

    void traverseMapAndReplaceAllVariablesWithValues(Map<String, Object> map, Map<String, Object> localValues,
                                                     Map<String, List<Object>> globalValues) {
        traverseMapAndReplaceAllVariablesWithValues(map, localValues, globalValues, null);
    }

    void traverseMapAndReplaceAllVariablesWithValues(Map<String, Object> map, Map<String, Object> localValues,
                                                     Map<String, List<Object>> globalValues,
                                                     Map<String, Object> additionalInputValues) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object object = entry.getValue();
            if (object instanceof String) {
                Object value = replaceVariablesWithValues((String) object, localValues, globalValues, additionalInputValues);
                map.put(key, value);
            } else if (object instanceof Map) {
                traverseMapAndReplaceAllVariablesWithValues((Map) object, localValues, globalValues, additionalInputValues);
            } else if (object instanceof List) {
                List list = (List) object;
                traverseListAndReplaceAllVariablesWithValues(list, localValues, globalValues, additionalInputValues);
            }
        }
    }

    private void traverseListAndReplaceAllVariablesWithValues(List list, Map<String, Object> localValues,
                                                              Map<String, List<Object>> globalValues,
                                                              Map<String, Object> additionalInputValues) {
        for (ListIterator iterator = list.listIterator(); iterator.hasNext(); ) {
            Object object = iterator.next();
            if (object instanceof String) {
                String source = (String) object;
                Object value = replaceVariablesWithValues(source, localValues, globalValues, additionalInputValues);
                if (source.endsWith(ALL_ENDING) && value instanceof List) {
                    iterator.remove();
                    List sublist = (List) value;
                    for (Object sublistValue : sublist) {
                        iterator.add(sublistValue);
                    }
                } else if (value == null) {
                    iterator.remove();
                } else {
                    iterator.set(value);
                }
            } else if (object instanceof Map) {
                traverseMapAndReplaceAllVariablesWithValues((Map) object, localValues, globalValues, additionalInputValues);
            } else if (object instanceof List) {
                traverseListAndReplaceAllVariablesWithValues((List) object, localValues, globalValues, additionalInputValues);
            }
        }
    }
}
