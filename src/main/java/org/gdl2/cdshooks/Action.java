package org.gdl2.cdshooks;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Action {
    private ActionType type;
    private String description;
    private Object resource;
    private UseTemplate resourceTemplate;

    public enum ActionType {
        @SerializedName("create")
        CREATE,

        @SerializedName("update")
        UPDATE,

        @SerializedName("delete")
        DELETE,
    }
}
