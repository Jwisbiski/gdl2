package org.gdl2.runtime;

import org.gdl2.model.Guideline;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.testng.AssertJUnit.fail;

public class HandleUnknownValuesTest extends TestCommon {
    private Interpreter interpreter;
    private Guideline guideline;
    private ArrayList<DataInstance> dataInstances;
    private List<DataInstance> result;

    @BeforeMethod
    public void setUp() {
        interpreter = new Interpreter();
        dataInstances = new ArrayList<>();
    }

    @Test
    public void can_skip_rule_with_null_with_one_condition() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_1.gdl2");
        assert_rule_skipped_without_exception();
    }

    @Test
    public void can_skip_rule_with_null_with_two_conditions() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_2.gdl2");
        dataInstances.add(toWeight("72.0,kg"));
        assert_rule_skipped_without_exception();
    }

    @Test
    public void can_skip_rule_with_null_with_one_condition_and_null_check() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_3.gdl2");
        dataInstances.add(toWeight("72.0,kg"));
        assert_rule_executed_without_exception();
    }

    @Test
    public void can_skip_rule_with_null_logicAnd_one_met_condition() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_3.gdl2");
        dataInstances.add(toWeight("72.0,kg"));
        assert_rule_executed_without_exception();
    }

    @Test
    public void can_execute_rule_with_no_null_values_two_conditions() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_2.gdl2");
        dataInstances.add(toWeight("72.0,kg"));
        dataInstances.add(toHeight("180.0,cm"));
        assert_rule_executed_without_exception();
    }

    @Test
    public void can_execute_rule_with_logic_or_true_unknown_value() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_4.gdl2");
        dataInstances.add(toWeight("72.0,kg"));
        assert_rule_executed_without_exception();
    }

    @Test
    public void can_execute_rule_with_logic_or_unknown_value_true() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_4.gdl2");
        dataInstances.add(toHeight("180.0,cm"));
        assert_rule_executed_without_exception();
    }

    @Test
    public void can_skip_rule_with_logic_or_false_unknown_value() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_4.gdl2");
        dataInstances.add(toWeight("52.0,kg"));
        assert_rule_skipped_without_exception();
    }

    @Test
    public void can_skip_rule_with_logic_or_unknown_value_false() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_4.gdl2");
        dataInstances.add(toHeight("150.0,cm"));
        assert_rule_skipped_without_exception();
    }

    @Test
    public void can_skip_rule_with_logic_and_true_unknown_value() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_5.gdl2");
        dataInstances.add(toWeight("72.0,kg"));
        assert_rule_skipped_without_exception();
    }

    @Test
    public void can_skip_rule_with_logic_and_false_unknown_value() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_5.gdl2");
        dataInstances.add(toWeight("52.0,kg"));
        assert_rule_skipped_without_exception();
    }

    @Test
    public void can_skip_rule_with_logic_and_unknown_value_true() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_5.gdl2");
        dataInstances.add(toHeight("180.0,cm"));
        assert_rule_skipped_without_exception();
    }

    @Test
    public void can_skip_rule_with_logic_and_unknown_value_false() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_5.gdl2");
        dataInstances.add(toHeight("150.0,cm"));
        assert_rule_skipped_without_exception();
    }

    @Test
    public void can_execute_rule_with_2_logic_or_unknown_false_true() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_6.gdl2");
        dataInstances.add(toHeight("150.0,cm"));
        assert_rule_executed_without_exception();
    }

    @Test
    public void can_execute_rule_with_nested_logic_or_multiple_unknown_values_true() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_7.gdl2");
        dataInstances.add(toHeight("170.0,cm"));
        assert_rule_executed_without_exception();
    }

    @Test
    public void can_skip_rule_with_nested_logic_or_multiple_unknown_values_false() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_7.gdl2");
        dataInstances.add(toHeight("150.0,cm"));
        assert_rule_skipped_without_exception();
    }

    private void assert_rule_executed_without_exception() {
        try {
            result = interpreter.executeSingleGuideline(guideline, dataInstances);
            assertThat(result.size(), is(1));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Should not throw exception due to null values in rule conditions");
        }
    }

    private void assert_rule_skipped_without_exception() {
        try {
            result = interpreter.executeSingleGuideline(guideline, dataInstances);
            assertThat(result.size(), is(0));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Should not throw exception due to null values in rule conditions");
        }
    }
}
