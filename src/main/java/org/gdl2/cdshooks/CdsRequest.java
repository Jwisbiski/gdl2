package org.gdl2.cdshooks;

import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
public class CdsRequest {
    private String hookInstance;
    private String fhirServer;
    private String hook;
    private String redirect;
    private String user;
    private String patient;
    private List<Object> context;
    private Map<String, Object> prefetch;
}
