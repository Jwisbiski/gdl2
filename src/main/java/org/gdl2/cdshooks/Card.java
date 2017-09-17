package org.gdl2.cdshooks;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Card {
    private String summary;
    private String detail;
    private IndicatorEnum indicator;
    private Source source;
    private List<Suggestion> suggestions;
    private List<Link> links;

    public enum IndicatorEnum {
        @SerializedName("info")
        INFO,

        @SerializedName("warning")
        WARNING,

        @SerializedName("hard-stop")
        HARD_STOP
    }
}
