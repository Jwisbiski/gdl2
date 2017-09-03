package org.gdl2.model;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

@Value
public class InternalVariable {
    private String id;
    private DataType dataType;

    public enum DataType {
        @SerializedName("quantity")
        QUANTITY,

        @SerializedName("count")
        COUNT,

        @SerializedName("text")
        TEXT,

        @SerializedName("coded_text")
        CODED_TEXT,

        @SerializedName("ordinal")
        ORDINAL
    }
}
