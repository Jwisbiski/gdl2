package org.gdl2.runtime;

import com.google.gson.Gson;
import org.gdl2.datatypes.DvOrdinal;
import org.gdl2.model.Guideline;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MultipleGuidelinesUseTemplateTest extends TestCommon {
    private Guideline guideline;
    private Interpreter interpreter;
    private List<DataInstance> input;
    private List<DataInstance> output;
    private Gson gson = new Gson();

    @BeforeMethod
    public void setUp() {
        input = new ArrayList<>();
        interpreter = new Interpreter();
    }

    @Test
    public void can_find_object_created_by_use_template_previous_guideline() throws Exception {
        List<Guideline> guidelines = new ArrayList<>();
        guidelines.add(loadGuideline("use_template_with_ordinal_test3.v0.1.gdl2"));
        guidelines.add(loadGuideline("use_template_with_ordinal_test4.v0.1.gdl2"));
        guidelines.add(loadGuideline("use_template_with_ordinal_test3.v0.1.gdl2"));
        guidelines.add(loadGuideline("use_template_with_ordinal_test5.v0.1.gdl2"));
        output = interpreter.executeGuidelines(guidelines, input);
        DvOrdinal dvOrdinal = gson.fromJson(gson.toJson(output.get(3).getRoot()), DvOrdinal.class);
        assertThat(dvOrdinal.toString(), is("5|ATC::C10AA05|atorvastatin|"));
    }
}