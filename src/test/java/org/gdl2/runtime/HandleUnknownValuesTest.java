package org.gdl2.runtime;

import org.gdl2.datatypes.DvQuantity;
import org.gdl2.model.Guideline;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
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
    }

    @Test
    public void can_skip_rule_with_null_with_one_condition() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_1.gdl2");
        dataInstances = new ArrayList<>(); // no input data
        try {
            interpreter.executeSingleGuideline(guideline, dataInstances);
        } catch (Exception e) {
            fail("Should not throw exception due to null values in rule conditions");
        }
    }

    @Test
    public void can_skip_rule_with_null_with_two_conditions() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_2.gdl2");
        dataInstances = new ArrayList<>();
        dataInstances.add(toWeight("72.0,kg"));
        try {
            result = interpreter.executeSingleGuideline(guideline, dataInstances);
        } catch (Exception e) {
            fail("Should not throw exception due to null values in rule conditions");
        }
    }

    @Test
    public void can_skip_rule_with_null_with_one_condition_and_null_check() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_3.gdl2");
        dataInstances = new ArrayList<>();
        dataInstances.add(toWeight("72.0,kg"));
        try {
            result = interpreter.executeSingleGuideline(guideline, dataInstances);
            assertThat(result.size(), is(1));
        } catch (Exception e) {
            fail("Should not throw exception due to null values in rule conditions");
        }
    }

    @Test
    public void can_skip_rule_with_null_logicAnd_one_met_condition() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_3.gdl2");
        dataInstances = new ArrayList<>();
        dataInstances.add(toWeight("72.0,kg"));
        try {
            result = interpreter.executeSingleGuideline(guideline, dataInstances);
            assertThat(result.size(), is(1));
        } catch (Exception e) {
            fail("Should not throw exception due to null values in rule conditions");
        }
    }


    @Test
    public void can_execute_rule_with_no_null_values_two_conditions() throws Exception {
        guideline = loadGuideline("handle_unknown_values_test_2.gdl2");
        dataInstances = new ArrayList<>();
        dataInstances.add(toWeight("72.0,kg"));
        dataInstances.add(toHeight("180.0,cm"));
        try {
            result = interpreter.executeSingleGuideline(guideline, dataInstances);
            assertThat(result.size(), is(1));
            DataInstance dataInstance = result.get(0);
            DvQuantity dvQuantity = dataInstance.getDvQuantity("/data[at0001]/events[at0002]/data[at0003]/items[at0004]");
            assertThat(dvQuantity.getMagnitude(), closeTo(1.90, 0.1));
            assertThat(dvQuantity.getPrecision(), is(2));
            assertThat(dvQuantity.getUnit(), is("m2"));
        } catch (Exception e) {
            fail("Should not throw exception due to null values in rule conditions");
        }
    }
}
