package org.gdl2.cdshooks;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
public class Suggestion {
    @Builder.Default
    private UUID uuid = UUID.randomUUID();
    private String label;
    private List<Object> create;
    private List<Object> delete;
}
