package org.gdl2.cdshooks;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Value;

import java.net.URL;

@Value
@Builder
public class Link {
    private String label;
    private URL url;
    private LinkType type;

    public enum LinkType {
        @SerializedName("absolute")
        ABSOLUTE,

        @SerializedName("smart")
        SMART
    }
}
