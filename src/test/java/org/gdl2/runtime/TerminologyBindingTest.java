package org.gdl2.runtime;

import org.gdl2.datatypes.DvCodedText;
import org.gdl2.datatypes.DvDateTime;
import org.gdl2.expression.ExpressionItem;
import org.gdl2.model.Guideline;
import org.gdl2.terminology.ReadV2SubsumptionEvaluator;
import org.gdl2.terminology.SubsumptionEvaluator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.gdl2.terminology.ReadV2SubsumptionEvaluator.UKTC_READ_V2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TerminologyBindingTest extends TestCommon {
    private Interpreter interpreter;
    private ExpressionItem expression;
    private Guideline guide;
    private HashMap<String, List<Object>> inputMap;
    private Object value;

    @BeforeMethod
    public void setUp() throws Exception {
        Map<String, SubsumptionEvaluator> subsumptionEvaluatorMap = new HashMap<>();
        subsumptionEvaluatorMap.put(UKTC_READ_V2, new ReadV2SubsumptionEvaluator());
        RuntimeConfiguration runtimeConfiguration = RuntimeConfiguration.builder()
                .currentDateTime(new DvDateTime())
                .language("en")
                .objectCreatorPlugin(new DefaultObjectCreator())
                .terminologySubsumptionEvaluators(subsumptionEvaluatorMap)
                .build();
        interpreter = new Interpreter(runtimeConfiguration);
        inputMap = new HashMap<>();
        value = null;
    }

    @Test
    public void can_check_isa_relationship_using_local_term_bindings_expected_true() throws Exception {
        guide = loadGuideline("CHA2DS2VASc_diagnosis_review.v1.0.1.gdl2");
        inputMap.put("gt0040", asList(new DvCodedText("Diabetes Type-1", "ICD10", "E10")));
        expression = parseExpression("$gt0040 is_a local::gt0102|Diabetes|");
        value = interpreter.evaluateExpressionItem(expression, inputMap, guide, null);
        assertThat(value, is(true));
    }

    @Test
    public void can_check_isa_relationship_using_local_term_bindings_expected_false() throws Exception {
        guide = loadGuideline("CHA2DS2VASc_diagnosis_review.v1.0.1.gdl2");
        inputMap.put("gt0040", asList(new DvCodedText("Heart failure", "ICD10", "I50")));
        expression = parseExpression("$gt0040 is_a local::gt0102|Diabetes|");
        value = interpreter.evaluateExpressionItem(expression, inputMap, guide, null);
        assertThat(value, is(false));
    }

    @Test
    public void can_check_isa_relationship_with_read_codes_v2_three_bytes_expect_true() throws Exception {
        guide = loadGuideline("ReadV2_test.v1.gdl2");
        inputMap.put("gt0040", asList(
                new DvCodedText("Acute congestive heart failure", "UKTC_READ_V2", "G5800")));
        expression = parseExpression("$gt0040 is_a local::gt0100|Heart failure|");
        value = interpreter.evaluateExpressionItem(expression, inputMap, guide, null);
        assertThat(value, is(true));
    }

    @Test
    public void can_check_isa_relationship_with_read_codes_v2_four_bytes_expect_true() throws Exception {
        guide = loadGuideline("ReadV2_test.v1.gdl2");
        inputMap.put("gt0040", asList(
                new DvCodedText("Acute congestive heart failure", "UKTC_READ_V2", "G5800")));
        expression = parseExpression("$gt0040 is_a local::gt0101|Congestive heart failure|");
        value = interpreter.evaluateExpressionItem(expression, inputMap, guide, null);
        assertThat(value, is(true));
    }

    @Test
    public void can_check_isa_relationship_with_read_codes_v2_code_list_expect_true() throws Exception {
        guide = loadGuideline("ReadV2_test.v1.gdl2");
        inputMap.put("gt0040", asList(
                new DvCodedText("Acute congestive heart failure", "UKTC_READ_V2", "G5800")));
        expression = parseExpression("$gt0040 is_a local::gt0102|Congestive heart failure|");
        value = interpreter.evaluateExpressionItem(expression, inputMap, guide, null);
        assertThat(value, is(true));
    }

    @Test
    public void can_check_isa_relationship_with_read_codes_v2() throws Exception {
        guide = loadGuideline("ReadV2_test.v1.gdl2");
        inputMap.put("gt0040", asList(
                new DvCodedText("Acute congestive heart failure", "UKTC_READ_V2", "G5700")));
        expression = parseExpression("$gt0040 is_a local::gt0100|Congestive heart failure|");
        value = interpreter.evaluateExpressionItem(expression, inputMap, guide, null);
        assertThat(value, is(false));
    }
}