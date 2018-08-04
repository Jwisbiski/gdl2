package org.gdl2.runtime;

import org.gdl2.model.Guideline;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

public class FiredRulesTest extends TestCommon {
    private static final String BSA_CALCULATION_FIRED_RULE = "BSA_Calculation_fired_rule.v1.gdl2";
    private Interpreter interpreter;
    private Guideline guideline;

    @BeforeMethod
    public void setUp() {
        interpreter = new Interpreter();
    }

    @Test
    public void can_evaluate_fired_rule_expected_true() throws Exception {
        guideline = loadGuideline(BSA_CALCULATION_FIRED_RULE);
        ArrayList<DataInstance> dataInstances = new ArrayList<>();
        dataInstances.add(toWeight("72.0,kg"));
        dataInstances.add(toHeight("180.0,cm"));

        Map<String, List<Object>> result = interpreter.execute(guideline, dataInstances).getResult();
        Object dataValue = result.get("gt0014").get(0);
        assertThat(dataValue, instanceOf(boolean.class));
        Boolean dvBoolean = (Boolean) dataValue;
        assertThat(dvBoolean.booleanValue(), is(true));
    }

    @Test
    public void can_evaluate_fired_rule_expected_false() throws Exception {
        guideline = loadGuideline(BSA_CALCULATION_FIRED_RULE);
        ArrayList<DataInstance> dataInstances = new ArrayList<>();
        dataInstances.add(toWeight("158.7,lbs"));
        dataInstances.add(toHeight("5.95,ft"));

        Map<String, List<Object>> result = interpreter.execute(guideline, dataInstances).getResult();
        Object dataValue = result.get("gt0014").get(0);
        assertThat(dataValue, instanceOf(boolean.class));
        Boolean dvBoolean = (Boolean) dataValue;
        assertThat(dvBoolean.booleanValue(), is(false));
    }
}
