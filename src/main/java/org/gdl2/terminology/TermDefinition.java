package org.gdl2.terminology;

import lombok.Value;

import java.util.Map;

@Value
public final class TermDefinition {
    private String id;
    private Map<String, Term> terms;

    public String getTermText(String key) {
        Term term = terms.get(key);
        return term == null ? null : terms.get(key).getText();
    }
}