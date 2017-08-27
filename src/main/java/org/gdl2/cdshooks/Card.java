package org.gdl2.cdshooks;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
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
        HARD_STOP;
    }
}
