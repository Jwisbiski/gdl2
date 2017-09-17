package org.gdl2.cdshooks;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Suggestion {
    private UUID uuid;
    private String label;
    private List<Action> actions;
}
