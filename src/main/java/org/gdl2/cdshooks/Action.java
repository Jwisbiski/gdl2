package org.gdl2.cdshooks;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Action {
    private ActionType type;
    private String description;
    private Object resource;
    private UseTemplate resourceTemplate;

    public enum ActionType {
        @JsonProperty("create")
        @SerializedName("create")
        CREATE,

        @JsonProperty("update")
        @SerializedName("update")
        UPDATE,

        @JsonProperty("delete")
        @SerializedName("delete")
        DELETE,
    }
}
