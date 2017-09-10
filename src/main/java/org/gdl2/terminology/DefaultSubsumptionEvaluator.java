package org.gdl2.terminology;

import java.util.Collections;
import java.util.Set;

/**
 * Default implementation of SubsumptionEvaluator using code string comparison.
 */
public class DefaultSubsumptionEvaluator implements SubsumptionEvaluator {

    @Override
    public Set<String> supportedTerminologies() {
        return Collections.emptySet();
    }

    @Override
    public boolean isA(String codeA, String codeB) {
        return codeA.startsWith(codeB);
    }
}