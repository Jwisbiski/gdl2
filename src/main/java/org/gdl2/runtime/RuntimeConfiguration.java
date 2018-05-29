package org.gdl2.runtime;

import lombok.Builder;
import lombok.Value;
import org.gdl2.datatypes.DvDateTime;
import org.gdl2.terminology.SubsumptionEvaluator;

import java.util.Map;

@Value
@Builder
public class RuntimeConfiguration {
    private String language;
    private DvDateTime currentDateTime;
    private boolean includingInputWithPredicate;
    private ObjectCreatorPlugin objectCreatorPlugin;
    private String dateTimeFormatPattern;
    private Map<String, SubsumptionEvaluator> terminologySubsumptionEvaluators;
}
