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

public class CdsHooksGuidelinesTest extends TestCommon {
    private Interpreter interpreter;
    private List<Guideline> guidelineList;

    @BeforeMethod
    public void setUp() throws Exception {
        interpreter = new Interpreter();
        guidelineList = new ArrayList<>();
    }

    @Test
    public void can_run_body_surface_calculation_rule_as_single_guideline() throws Exception {
        guidelineList.add(loadGuideline("BSA_Calculation_cdshooks_test.v1.gdl2"));
        ArrayList<DataInstance> dataInstances = new ArrayList<>();
        dataInstances.add(toWeight("72.0,kg"));
        dataInstances.add(toHeight("180.0,cm"));

        List<DataInstance> result = interpreter.executeGuidelines(guidelineList, dataInstances);
        DataInstance dataInstance = result.get(0);
        DvQuantity dvQuantity = dataInstance.getDvQuantity("/data[at0001]/events[at0002]/data[at0003]/items[at0004]");
        assertThat(dvQuantity.getMagnitude(), closeTo(1.90, 0.1));
        assertThat(dvQuantity.getPrecision(), is(2));
        assertThat(dvQuantity.getUnits(), is("m2"));
    }
}
