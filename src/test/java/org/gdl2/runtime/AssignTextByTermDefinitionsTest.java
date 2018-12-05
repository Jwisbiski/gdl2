package org.gdl2.runtime;

import org.gdl2.model.Guideline;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AssignTextByTermDefinitionsTest extends TestCommon {
    private Interpreter interpreter;
    private ArrayList<DataInstance> dataInstances = new ArrayList<>();

    @Test
    public void can_assign_coded_text_value_with_english() throws Exception {
        Guideline guideline = loadGuideline("assign_text_with_term_definitions.gdl2");
        interpreter = new Interpreter("en");
        List<DataInstance> result = interpreter.executeSingleGuideline(guideline, dataInstances);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).get("/data[at0001]"), is("english"));
    }

    @Test
    public void can_assign_coded_text_value_with_swedish() throws Exception {
        Guideline guideline = loadGuideline("assign_text_with_term_definitions.gdl2");
        interpreter = new Interpreter("sv");
        List<DataInstance> result = interpreter.executeSingleGuideline(guideline, dataInstances);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).get("/data[at0001]"), is("svensk"));
    }

    @Test(expectedExceptions =  IllegalArgumentException.class)
    public void can_assign_coded_text_value_with_unknown_language_expect_exception() throws Exception {
        Guideline guideline = loadGuideline("assign_text_with_term_definitions.gdl2");
        interpreter = new Interpreter("da");
        List<DataInstance> result = interpreter.executeSingleGuideline(guideline, dataInstances);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).get("/data[at0001]"), is("english"));
    }

    @Test(expectedExceptions =  IllegalArgumentException.class)
    public void can_assign_coded_text_value_with_unknown_term_expect_exception() throws Exception {
        Guideline guideline = loadGuideline("assign_text_with_term_definitions2.gdl2");
        interpreter = new Interpreter("en");
        List<DataInstance> result = interpreter.executeSingleGuideline(guideline, dataInstances);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).get("/data[at0001]"), is("english"));
    }
}
