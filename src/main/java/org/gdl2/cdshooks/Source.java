package org.gdl2.cdshooks;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.net.URL;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Source {
    private String label;
    private URL url;
    private URL icon;
    private String labelReference;
    private String urlReference;
}