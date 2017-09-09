package org.gdl2.runtime;

import lombok.Builder;
import lombok.Value;
import org.gdl2.datatypes.DvDateTime;

@Value
@Builder
public class RuntimeConfiguration {
    private DvDateTime currentDateTime;
    private ObjectCreatorPlugin objectCreatorPlugin;
    private String language;
}
