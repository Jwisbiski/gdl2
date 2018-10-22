package org.gdl2.runtime;

import org.gdl2.datatypes.DvQuantity;
import org.gdl2.model.Guideline;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

public class BodySurfaceAreaCalculationTest extends TestCommon {
    private Interpreter interpreter;
    private Guideline guideline;
    private ArrayList<DataInstance> dataInstances;
    private List<DataInstance> result;

    @BeforeMethod
    public void setUp() {
        interpreter = new Interpreter();
    }

    @Test
    public void can_run_body_surface_calculation_rule_as_single_guideline() throws Exception {
        executeGuidelineAndVerifyResult(BSA_CALCULATION);
    }

    private void executeGuidelineAndVerifyResult(String guidelineId) throws Exception {
        guideline = loadGuideline(guidelineId);
        dataInstances = new ArrayList<>();
        dataInstances.add(toWeight("72.0,kg"));
        dataInstances.add(toHeight("180.0,cm"));

        result = interpreter.executeSingleGuideline(guideline, dataInstances);
        assertThat(result.size(), is(1));
        DataInstance dataInstance = result.get(0);
        DvQuantity dvQuantity = dataInstance.getDvQuantity("/data[at0001]/events[at0002]/data[at0003]/items[at0004]");
        assertThat(dvQuantity.getMagnitude(), closeTo(1.90, 0.1));
        assertThat(dvQuantity.getPrecision(), is(2));
        assertThat(dvQuantity.getUnit(), is("m2"));
    }

    @Test
    public void can_run_body_surface_calculation_using_long_expression() throws Exception {
        executeGuidelineAndVerifyResult("BSA_Calculation_long_expression_test.v1.gdl2");
    }

    @Test
    public void can_run_body_surface_calculation_rule_as_guidelines() throws Exception {
        executeGuidelineAndVerifyResult(BSA_CALCULATION);
        dataInstances = new ArrayList<>();
        dataInstances.add(toWeight("72.0,kg"));
        dataInstances.add(toHeight("180.0,cm"));

        result = interpreter.executeGuidelines(Collections.singletonList(guideline), dataInstances);
        DataInstance dataInstance = result.get(0);
        assertThat(dataInstance.id(), is("gt0019"));
        DvQuantity dvQuantity = dataInstance.getDvQuantity("/data[at0001]/events[at0002]/data[at0003]/items[at0004]");
        assertThat(dvQuantity.getMagnitude(), closeTo(1.90, 0.1));
        assertThat(dvQuantity.getPrecision(), is(2));
        assertThat(dvQuantity.getUnit(), is("m2"));
    }

    @Test
    public void can_run_body_surface_calculation_rule_without_when_statements() throws Exception {
        guideline = loadGuideline(BSA_CALCULATION_WITHOUT_WHEN);
        dataInstances = new ArrayList<>();
        dataInstances.add(toWeight("72.0,kg"));
        dataInstances.add(toHeight("180.0,cm"));

        result = interpreter.executeSingleGuideline(guideline, dataInstances);
        DataInstance dataInstance = result.get(0);
        DvQuantity dvQuantity = dataInstance.getDvQuantity("/data[at0001]/events[at0002]/data[at0003]/items[at0004]");
        assertThat(dvQuantity.getMagnitude(), closeTo(1.90, 0.1));
        assertThat(dvQuantity.getPrecision(), is(2));
        assertThat(dvQuantity.getUnit(), is("m2"));
    }
}
