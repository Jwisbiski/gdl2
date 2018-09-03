package org.gdl2.runtime;

import lombok.Builder;
import lombok.Value;
import org.gdl2.terminology.SubsumptionEvaluator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@Value
@Builder
public class RuntimeConfiguration {
    private String language;
    private ZoneId timezoneId;
    private ZonedDateTime currentDateTime;
    private boolean includingInputWithPredicate;
    private ObjectCreatorPlugin objectCreatorPlugin;
    private String dateTimeFormatPattern;
    private Map<String, SubsumptionEvaluator> terminologySubsumptionEvaluators;
}
