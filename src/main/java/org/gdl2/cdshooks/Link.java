package org.gdl2.cdshooks;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Value;

import java.net.URL;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Link {
    private String label;
    private URL url;
    private LinkType type;
    private String labelReference;
    private String urlReference;

    public enum LinkType {
        @JsonProperty("absolute")
        @SerializedName("absolute")
        ABSOLUTE,

        @JsonProperty("smart")
        @SerializedName("smart")
        SMART
    }
}
