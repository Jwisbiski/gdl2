package org.gdl2.cdshooks;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Decision {
    private List create;
    private List delete;
}
