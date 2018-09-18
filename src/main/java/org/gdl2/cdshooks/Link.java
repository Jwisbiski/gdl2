package org.gdl2.cdshooks;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

import java.net.URL;

/* One of the requirements of cds-service, is to
   be able to override a link's url,
   thus Link class uses the @Data annotation instead of @Value
 */
@Data
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
        SMART,

        @JsonProperty("app")
        @SerializedName("app")
        APP
    }
}
