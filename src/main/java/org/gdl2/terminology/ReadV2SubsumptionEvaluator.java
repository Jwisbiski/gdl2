package org.gdl2.terminology;

import java.util.Collections;
import java.util.Set;

/**
 * Subsumption evaluator implementation for Read Code v2.
 */
public class ReadV2SubsumptionEvaluator implements SubsumptionEvaluator {
    public static final String UKTC_READ_V2 = "UKTC_READ_V2";
    private static final Set<String> SUPPORTED_TERMINOLOGIES = Collections.singleton(UKTC_READ_V2);

    @Override
    public Set<String> supportedTerminologies() {
        return SUPPORTED_TERMINOLOGIES;
    }

    @Override
    public boolean isA(String codeA, String codeB) {
        if (codeB.indexOf('.') > 0) {
            codeB = codeB.substring(0, codeB.indexOf('.'));
        }
        return codeA.startsWith(codeB);
    }
}
