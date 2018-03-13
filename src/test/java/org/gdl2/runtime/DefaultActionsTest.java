package org.gdl2.runtime;

import org.gdl2.datatypes.DvQuantity;
import org.gdl2.model.Guideline;
import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DefaultActionsTest extends TestCommon {
    private Interpreter interpreter;

    @BeforeMethod
    public void setUp() {
        interpreter = new Interpreter();
    }

    @Test
    public void can_run_default_actions() throws Exception {
        Guideline guideline = loadGuideline("Default_action_test");
        Map<String, List<Object>> result = interpreter.execute(guideline, new ArrayList<>());
        Object dataValue = result.get("gt0013").get(0);
        assertThat(dataValue, Matchers.instanceOf(DvQuantity.class));
        DvQuantity dvQuantity = (DvQuantity) dataValue;
        assertThat(dvQuantity.getUnit(), is("m2"));
    }

    @Test
    public void can_run_default_actions_expect_assigned_data_instance() throws Exception {
        Guideline guideline = loadGuideline("Default_action_test");
        List<Guideline> guidelineList = new ArrayList<>();
        guidelineList.add(guideline);
        List<DataInstance> dataInstanceList = interpreter.executeGuidelines(guidelineList, new ArrayList<>());
        assertThat(dataInstanceList.size(), is(1));
    }

    @Test
    public void can_run_default_actions_expect_assigned_datetime() throws Exception {
        Guideline guideline = loadGuideline("Default_action_test2");
        List<Guideline> guidelineList = new ArrayList<>();
        guidelineList.add(guideline);
        List<DataInstance> dataInstanceList = interpreter.executeGuidelines(guidelineList, new ArrayList<>());
        assertThat(dataInstanceList.size(), is(1));
    }
}
