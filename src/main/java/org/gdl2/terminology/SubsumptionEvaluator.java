package org.gdl2.terminology;

import java.util.Set;

/**
 * Interface for terminology subsumption relation check.
 */
public interface SubsumptionEvaluator {

    /**
     * The identifiers of the terminology this evaluator implementation supports.
     *
     * @return identifiers of terminology
     */
    Set<String> supportedTerminologies();

    /**
     * Check is_a (subsumption) relation between codeA and codeB.
     *
     * @param codeA the code
     * @param codeB the code
     * @return true if codeA is_a codeB
     */
    boolean isA(String codeA, String codeB);
}
