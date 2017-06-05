package org.gdl2.cdshooks;

import lombok.Builder;
import lombok.Value;

import java.net.URL;

@Value
@Builder
public class Source {
    private String label;
    private URL url;
    private String labelReference;
    private String urlReference;
}