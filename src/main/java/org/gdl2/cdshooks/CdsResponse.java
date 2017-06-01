package org.gdl2.cdshooks;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CdsResponse {
    private List<Card> cards;
    private List<Decision> decisions;
}
