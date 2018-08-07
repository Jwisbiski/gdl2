package org.gdl2.runtime;

import org.gdl2.datatypes.*;
import org.gdl2.expression.*;
import org.gdl2.model.Guideline;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static javax.xml.bind.DatatypeConverter.parseDateTime;
import static org.gdl2.runtime.Interpreter.CURRENT_DATETIME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PredicateTest extends TestCommon {
    private String archetypeId;
    private Interpreter interpreter;
    private Guideline guideline;

    @BeforeMethod
    public void setUp() {
        archetypeId = "archetype";
        interpreter = new Interpreter();
    }

    @Test
    public void can_evaluate_predicate_with_max_function_expected_one_result() {
        // predicates = <"max(/data[at0001]/items[at0004])",...>
        String path = "/data[at0001]/items[at0004]";
        ExpressionItem predicate = new UnaryExpression(
                new Variable(null, "name", path, null), OperatorKind.MAX);
        DataInstance[] dataInstances = new DataInstance[4];
        dataInstances[0] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue("gt0011", new DvCount(1))
                .addValue(("/data[at0001]/items[at0004]"), ZonedDateTime.parse("2010-01-01T00:00:00Z"))
                .build();
        dataInstances[1] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue("gt0011", new DvCount(2))
                .addValue(("/data[at0001]/items[at0004]"), ZonedDateTime.parse("2012-01-01T00:00:00Z"))
                .build();
        dataInstances[2] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue("gt0011", new DvCount(8))
                .addValue(("/data[at0001]/items[at0004]"), ZonedDateTime.parse("2015-10-01T00:00:00Z"))
                .build();
        dataInstances[3] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue("gt0011", new DvCount(5))
                .addValue(("/data[at0001]/items[at0004]"), ZonedDateTime.parse("2013-01-01T00:00:00Z"))
                .build();
        List<DataInstance> result = interpreter.evaluateDataInstancesWithPredicate(Arrays.asList(dataInstances),
                predicate, null);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getDvCount("gt0011").getMagnitude(), is(8));
    }

    @Test
    public void can_evaluate_predicate_with_min_function_expected_one_result() {
        // predicates = <"max(/data[at0001]/items[at0004])",...>
        String path = "/data[at0001]/items[at0004]";
        ExpressionItem predicate = new UnaryExpression(
                new Variable(null, "name", path, null), OperatorKind.MIN);
        DataInstance[] dataInstances = new DataInstance[3];
        dataInstances[0] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue("gt0011", new DvCount(1))
                .addValue(path, ZonedDateTime.parse("2010-01-01T00:00:00Z"))
                .build();
        dataInstances[1] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue("gt0011", new DvCount(2))
                .addValue(path, ZonedDateTime.parse("2012-01-01T00:00:00Z"))
                .build();
        dataInstances[2] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue("gt0011", new DvCount(8))
                .addValue(path, ZonedDateTime.parse("2015-10-01T00:00:00Z"))
                .build();
        List<DataInstance> result = interpreter.evaluateDataInstancesWithPredicate(Arrays.asList(dataInstances),
                predicate, null);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getDvCount("gt0011").getMagnitude(), is(1));
    }

    @Test
    public void can_evaluate_predicate_with_is_a_local_term_bindings() throws Exception {
        // predicates = <"/data[at0001]/items[at0002.1] is_a local::gt0101|Hypertension|",...>
        String path = "/data[at0001]/items[at0002.1]";
        guideline = loadGuideline("CHA2DS2VASc_diagnosis_review.v1.0.1.gdl2");
        ExpressionItem predicate = new BinaryExpression(new Variable("code", "name", path, "attribute"),
                new CodedTextConstant("Hypertension", new CodePhrase("local", "gt0101")), OperatorKind.IS_A);
        DataInstance[] dataInstances = new DataInstance[5];
        dataInstances[0] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue("gt0011", new DvCount(1))
                .addValue(path, new DvCodedText("Hypertension", "ICD10", "I10"))
                .build();
        dataInstances[1] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue("gt0011", new DvCount(3))
                .addValue(path, new DvCodedText("Diabetes Type-1", "ICD10", "E10"))
                .build();
        dataInstances[2] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue("gt0011", new DvCount(5))
                .addValue(path, new DvCodedText("Hypertension", "ICD10", "I11"))
                .build();
        dataInstances[3] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue("gt0011", new DvCount(7))
                .build();
        dataInstances[4] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue("gt0011", new DvCount(9))
                .addValue(path, new DvCodedText("Hypertension", "ICD10", "I11"))
                .build();
        List<DataInstance> result = interpreter.evaluateDataInstancesWithPredicate(Arrays.asList(dataInstances),
                predicate, guideline);
        assertThat(result.size(), is(3));
        assertThat(result.get(0).getDvCount("gt0011").getMagnitude(), is(1));
        assertThat(result.get(1).getDvCount("gt0011").getMagnitude(), is(5));
        assertThat(result.get(2).getDvCount("gt0011").getMagnitude(), is(9));
        assertThat(result.get(0).getDvCodedText(path).getDefiningCode().getCode(), is("I10"));
        assertThat(result.get(1).getDvCodedText(path).getDefiningCode().getCode(), is("I11"));
        assertThat(result.get(2).getDvCodedText(path).getDefiningCode().getCode(), is("I11"));
    }

    @Test
    public void can_evaluate_predicate_with_is_a_local_term_bindings_and_max_timestamp() throws Exception {
        // "/data[at0001]/items[at0002.1] is_a local::gt0043|Vascular disease diagnosis code|", "max(/data[at0001]/items[at0003])"
        String countCode = "gt0011";
        String codePath = "/data[at0001]/items[at0002.1]";
        String timestampPath = "/data[at0001]/items[at0003]";
        guideline = loadGuideline("Stroke_prevention_dashboard_case.v1.gdl2");
        List<ExpressionItem> predicates = guideline.getDefinition().getDataBindings().get("gt0059").getPredicates();

        DataInstance[] dataInstances = new DataInstance[5];
        dataInstances[0] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue(countCode, new DvCount(1))
                .addValue(codePath, new DvCodedText("Hypertension", "ICD10", "I10"))
                .addValue(timestampPath, ZonedDateTime.parse("2012-01-01T00:00:00Z"))
                .build();
        dataInstances[1] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue(countCode, new DvCount(3))
                .addValue(codePath, new DvCodedText("Other aneurysm", "ICD10", "I72"))        // right code
                .addValue(timestampPath, ZonedDateTime.parse("2011-01-01T00:00:00Z"))
                .build();
        dataInstances[2] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue(countCode, new DvCount(5))
                .addValue(codePath, new DvCodedText("Hypertension", "ICD10", "I11"))
                .addValue(timestampPath, ZonedDateTime.parse("2010-01-01T00:00:00Z"))
                .build();
        dataInstances[3] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue(countCode, new DvCount(7))
                .addValue(codePath, new DvCodedText("Hypertension", "ICD10", "I11"))             // wrong code
                .addValue(timestampPath, ZonedDateTime.parse("2014-01-01T00:00:00Z"))             // max date
                .build();
        dataInstances[4] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue(countCode, new DvCount(9))
                .addValue(codePath, new DvCodedText("myocardial infarction", "ICD10", "I21"))    // right code
                .addValue(timestampPath, ZonedDateTime.parse("2013-01-01T00:00:00Z"))
                .build();
        List<DataInstance> result = interpreter.evaluateDataInstancesWithPredicates(Arrays.asList(dataInstances),
                predicates, guideline);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getDvCount(countCode).getMagnitude(), is(9));
        assertThat(result.get(0).getDvCodedText(codePath).getDefiningCode().getCode(), is("I21"));
    }

    @Test
    public void can_evaluate_predicate_with_is_a_local_term_bindings_and_max_timestamp_expect_one() throws Exception {
        // "/data[at0001]/items[at0002.1] is_a local::gt0043|Vascular disease diagnosis code|", "max(/data[at0001]/items[at0003])"
        String countCode = "gt0011";
        String codePath = "/data[at0001]/items[at0002.1]";
        String timestampPath = "/data[at0001]/items[at0003]";
        guideline = loadGuideline("Stroke_prevention_dashboard_case_2.v1.gdl2");
        List<ExpressionItem> predicates = guideline.getDefinition().getDataBindings().get("gt0059").getPredicates();

        DataInstance[] dataInstances = new DataInstance[2];
        dataInstances[0] = new DataInstance.Builder() // wrong code but later date
                .modelId(archetypeId)
                .addValue(countCode, new DvCount(1))
                .addValue(codePath, new DvCodedText("Hypertension", "ICD10", "I10"))
                .addValue(timestampPath, ZonedDateTime.parse("2013-01-01T00:00:00Z"))
                .build();
        dataInstances[1] = new DataInstance.Builder()
                .modelId(archetypeId)
                .addValue(countCode, new DvCount(9))
                .addValue(codePath, new DvCodedText("myocardial infarction", "ICD10", "I21"))    // right code
                .addValue(timestampPath, ZonedDateTime.parse("2012-01-01T00:00:00Z"))
                .build();
        List<DataInstance> result = interpreter.evaluateDataInstancesWithPredicates(Arrays.asList(dataInstances),
                predicates, guideline);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getDvCount(countCode).getMagnitude(), is(9));
        assertThat(result.get(0).getDvCodedText(codePath).getDefiningCode().getCode(), is("I21"));
    }

    @Test
    public void can_run_empty_list_without_right_input_on_max_predicate() {
        DataInstance[] dataInstances = new DataInstance[1];
        dataInstances[0] = new DataInstance.Builder()
                .modelId("archetype")
                .addValue("count", new DvCount(1))
                .build();
        String path = "/data[at0001]/items[at0004]";
        ExpressionItem predicate = new UnaryExpression(
                new Variable(null, "name", path, null), OperatorKind.MAX);
        List<DataInstance> result = interpreter.evaluateDataInstancesWithPredicate(Arrays.asList(dataInstances), predicate, null);
        assertThat(result.size(), Matchers.is(0));
    }

    @Test
    public void can_run_empty_list_without_right_input_on_min_predicate() {
        DataInstance[] dataInstances = new DataInstance[1];
        dataInstances[0] = new DataInstance.Builder()
                .modelId("archetype")
                .addValue("count", new DvCount(1))
                .build();
        String path = "/data[at0001]/items[at0004]";
        ExpressionItem predicate = new UnaryExpression(
                new Variable(null, "name", path, null), OperatorKind.MIN);
        List<DataInstance> result = interpreter.evaluateDataInstancesWithPredicate(Arrays.asList(dataInstances), predicate, null);
        assertThat(result.size(), Matchers.is(0));
    }

    @Test
    public void can_evaluate_observation_datetime_against_current_datetime_minus_12_month() {
        // /data/events/time>=($currentDateTime-12,mo)
        DataInstance[] dataInstances = new DataInstance[1];
        dataInstances[0] = new DataInstance.Builder()
                .modelId("weight")
                .addValue("/data/events/time", ZonedDateTime.parse("2014-02-15T18:18:00Z"))
                .build();
        interpreter = new Interpreter(ZonedDateTime.parse("2015-01-10T00:00:00Z"));
        BinaryExpression binaryExpression = new BinaryExpression(
                new Variable(CURRENT_DATETIME, null, null, null),
                new QuantityConstant(new DvQuantity("mo", 12.0, 0)), OperatorKind.SUBTRACTION);
        BinaryExpression predicate = new BinaryExpression(Variable.createByPath("/data/events/time"),
                binaryExpression, OperatorKind.GREATER_THAN_OR_EQUAL);
        List<DataInstance> result = interpreter.evaluateDataInstancesWithPredicate(Arrays.asList(dataInstances), predicate, null);
        assertThat(result.size(), Matchers.is(1));
        assertThat(result.get(0).modelId(), is("weight"));
    }

    @Test
    public void can_evaluate_pathed_datetime_against_current_datetime_minus_12_month() {
        // /data[at0001]/items[at0003]/value/value>=($currentDateTime.value-12,mo)
        DataInstance[] dataInstances = new DataInstance[1];
        dataInstances[0] = new DataInstance.Builder()
                .modelId("weight")
                .addValue("/data[at0001]/items[at0003]", ZonedDateTime.parse("2014-02-15T18:18:00Z"))
                .build();
        interpreter = new Interpreter(ZonedDateTime.parse("2015-01-10T00:00:00Z"));
        BinaryExpression binaryExpression = new BinaryExpression(
                new Variable(CURRENT_DATETIME, null, null, null),
                new QuantityConstant(new DvQuantity("mo", 12.0, 0)), OperatorKind.SUBTRACTION);
        BinaryExpression predicate = new BinaryExpression(Variable.createByPath("/data[at0001]/items[at0003]/value/value"),
                binaryExpression, OperatorKind.GREATER_THAN_OR_EQUAL);
        List<DataInstance> result = interpreter.evaluateDataInstancesWithPredicate(Arrays.asList(dataInstances), predicate, null);
        assertThat(result.size(), Matchers.is(1));
        assertThat(result.get(0).modelId(), is("weight"));
    }
}