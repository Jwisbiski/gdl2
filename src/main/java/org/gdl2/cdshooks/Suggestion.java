package org.gdl2.cdshooks;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
public class Suggestion {
    private UUID uuid;
    private String label;
    private List<Action> actions;
}
