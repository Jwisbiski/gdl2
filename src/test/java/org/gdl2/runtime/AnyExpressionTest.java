package org.gdl2.runtime;

import org.gdl2.datatypes.DvCodedText;
import org.gdl2.datatypes.DvCount;
import org.gdl2.datatypes.DvDateTime;
import org.gdl2.datatypes.DvQuantity;
import org.gdl2.expression.AnyExpression;
import org.gdl2.expression.ExpressionItem;
import org.gdl2.expression.Variable;
import org.gdl2.model.Guideline;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Group of test cases related to evaluation of any expressions in rules
 */
public class AnyExpressionTest extends TestCommon {
    private Interpreter interpreter;
    private List<DataInstance> input;
    private List<DataInstance> output;
    private ExpressionItem expressionItem;
    private AnyExpression anyExpression;
    private HashMap<String, List<Object>> inputMap;
    private Object value;

    @BeforeMethod
    public void setUp() {
        interpreter = new Interpreter();
        input = new ArrayList<>();
        inputMap = new HashMap<>();
        value = null;
    }

    @Test
    public void can_retrieve_actual_matching_value_with_any_expression() throws Exception {
        List<Guideline> guidelines = loadSingleGuideline("any_expression_test.v1.gdl2");
        int[] firstValues = {10, 20, 33, 40, 50};
        int[] secondValues = {10, 20, 30, 40, 50};
        addToInputDataInstances("model_1", firstValues, secondValues);
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(1));
        DataInstance dataInstance = output.get(0);
        assertThat(dataInstance.get("/value_1"), is(33));
        assertThat(dataInstance.get("/value_2"), is(30));
    }

    @Test
    public void can_retrieve_actual_matching_value_with_any_expression2() throws Exception {
        List<Guideline> guidelines = loadSingleGuideline("any_expression_test2.v1.gdl2");
        int[] firstValues = {10, 20, 33, 40, 50};
        int[] secondValues = {10, 20, 30, 40, 50};
        int[] thirdValues = {10};
        int[] forthValues = {10};
        addToInputDataInstances("model_1", firstValues, secondValues);
        addToInputDataInstances("model_3", thirdValues, forthValues);
        output = interpreter.executeGuidelines(guidelines, input);
    }

    @Test
    public void can_execute_rules_each_with_any_expression() throws Exception {
        List<Guideline> guidelines = loadSingleGuideline("any_expression_test3.v1.gdl2");
        input.add(new DataInstance.Builder()
                .modelId("org.hl7.fhir.dstu3.model.Observation")
                .addValue("/valueQuantity", DvQuantity.valueOf("140,umol/L"))
                .addValue("/effectiveDateTime", ZonedDateTime.parse("2016-04-04T14:30:00Z"))
                .addValue("/code/coding[0]", DvCodedText.valueOf("NPU::NPU18016|label|"))
                .build());
        input.add(new DataInstance.Builder()
                .modelId("org.hl7.fhir.dstu3.model.Observation")
                .addValue("/valueQuantity", DvQuantity.valueOf("120,umol/L"))
                .addValue("/effectiveDateTime", ZonedDateTime.parse("2016-04-05T14:30:00Z"))
                .addValue("/code/coding[0]", DvCodedText.valueOf("NPU::NPU18016|label|"))
                .build());
        input.add(new DataInstance.Builder()
                .modelId("org.hl7.fhir.dstu3.model.Observation")
                .addValue("/valueQuantity", DvQuantity.valueOf("150,umol/L"))
                .addValue("/effectiveDateTime", ZonedDateTime.parse("2016-04-06T13:30:00Z"))
                .addValue("/code/coding[0]", DvCodedText.valueOf("NPU::NPU18016|label|"))
                .build());
        output = interpreter.executeGuidelines(guidelines, input);
    }

    private void addToInputDataInstances(String modelId, int[] firstValues, int[] secondValues) {
        for (int i = 0; i < firstValues.length; i++) {
            input.add(new DataInstance.Builder()
                    .modelId(modelId)
                    .addValue("/value_1", firstValues[i])
                    .addValue("/value_2", secondValues[i])
                    .build());
        }
    }

    @Test
    public void can_evaluate_any_function_with_single_value_expect_false() {
        expressionItem = parseExpression("any($gt0005>3)");
        inputMap.put("gt0005", asList(new DvCount(2)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    @Test
    public void can_evaluate_any_function_with_single_value_expect_true() {
        expressionItem = parseExpression("any($gt0005>3)");
        inputMap.put("gt0005", asList(new DvCount(5)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_evaluate_any_function_with_3_values_expect_false() {
        expressionItem = parseExpression("any($gt0005>3)");
        inputMap.put("gt0005",
                toDvCountList(new int[]{1, 3, 2}));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    @Test
    public void can_evaluate_any_function_with_5_values_expect_true() {
        expressionItem = parseExpression("any($gt0005>3)");
        inputMap.put("gt0005",
                toDvCountList(new int[]{1, 3, 2, 5, 1}));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_evaluate_any_function_with_two_variables_5_values_and_1_value_expect_true() {
        expressionItem = parseExpression("any[$gt0005]($gt0005>$gt0006)");
        inputMap.put("gt0005", toDvCountList(new int[]{1, 3, 2, 5, 1}));
        inputMap.put("gt0006", asList(DvCount.valueOf(4)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_evaluate_any_function_with_two_variables_reversed_order_5_values_and_1_value_expect_true() {
        expressionItem = parseExpression("any($gt0006<$gt0005)");
        inputMap.put("gt0005", toDvCountList(new int[]{1, 3, 2, 5, 1}));
        inputMap.put("gt0006", asList(DvCount.valueOf(4)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_get_variable_id_with_single_variable() {
        anyExpression = (AnyExpression) parseExpression("any($gt0005>3)");
        List<Variable> list = anyExpression.getInputVariables();
        assertThat(list.size(), is(1));
        assertThat(list.get(0).getCode(), is("gt0005"));
    }

    @Test
    public void can_get_variable_id_with_two_variables() {
        anyExpression = (AnyExpression) parseExpression("any(($gt1007-$gt1004)>($gt1004*0.5))");
        List<Variable> list = anyExpression.getInputVariables();
        assertThat(list.size(), is(2));
        assertThat(list.get(0).getCode(), is("gt1007"));
        assertThat(list.get(1).getCode(), is("gt1004"));
    }

    @Test
    public void can_get_variable_id_with_four_variables() {
        anyExpression = (AnyExpression) parseExpression("any((($gt1007-$gt1004)>($gt1004*0.5))&&($gt1008.value<($gt1005.value+7,d)))");
        List<Variable> list = anyExpression.getInputVariables();
        assertThat(list.size(), is(4));
        assertThat(list.get(0).getCode(), is("gt1007"));
        assertThat(list.get(1).getCode(), is("gt1004"));
        assertThat(list.get(2).getCode(), is("gt1008"));
        assertThat(list.get(3).getCode(), is("gt1005"));
    }

    @Test
    public void can_evaluate_any_function_with_2_variables_and_3_values_expect_true() {
        expressionItem = parseExpression("any(($gt1007-$gt1004)>($gt1004*0.5))");
        inputMap.put("gt1004", toDvCountList(new int[]{10, 30, 20}));
        inputMap.put("gt1007", toDvCountList(new int[]{12, 40, 35}));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_evaluate_any_function_with_2_variables_and_5_values_expect_false() {
        expressionItem = parseExpression("any(($gt1007-$gt1004)>($gt1004*0.5))");
        inputMap.put("gt1004", toDvCountList(new int[]{10, 30, 20, 50, 60}));
        inputMap.put("gt1007", toDvCountList(new int[]{12, 40, 18, 60, 89}));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    private List<Object> toDvCountList(int[] values) {
        List<Object> input = new ArrayList<>();
        for (int i : values) {
            input.add(DvCount.valueOf(i));
        }
        return input;
    }
}