package org.gdl2.runtime;

import org.gdl2.datatypes.DvCodedText;
import org.gdl2.model.Guideline;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class UseTargetLanguageTest extends TestCommon {
    private static final String PATH = "/data[at0002]/events[at0003]/data[at0001]/items[at0099]";
    private Interpreter interpreter;
    private List<Guideline> guidelineList;
    private ArrayList<DataInstance> dataInstances;

    @BeforeMethod
    public void setUp() throws Exception {
        interpreter = new Interpreter();
        guidelineList = new ArrayList<>();
        dataInstances = new ArrayList<>();
    }

    @Test
    public void can_get_term_in_english() throws Exception {
        interpreter = new Interpreter("en");
        guidelineList.add(loadGuideline("Use_target_language_term_test.v1.gdl2"));
        List<DataInstance> result = interpreter.executeGuidelines(guidelineList, dataInstances);
        DataInstance dataInstance = result.get(0);
        DvCodedText dvCodedText = dataInstance.getDvCodedText(PATH);
        assertThat(dvCodedText.getValue(), is("Five"));
    }

    @Test
    public void can_get_term_in_swedish() throws Exception {
        interpreter = new Interpreter("sv");
        guidelineList.add(loadGuideline("Use_target_language_term_test.v1.gdl2"));
        List<DataInstance> result = interpreter.executeGuidelines(guidelineList, dataInstances);
        DataInstance dataInstance = result.get(0);
        DvCodedText dvCodedText = dataInstance.getDvCodedText(PATH);
        assertThat(dvCodedText.getValue(), is("Fem"));
    }

    @Test
    public void can_get_term_in_english_with_missing_language() throws Exception {
        interpreter = new Interpreter("da");
        guidelineList.add(loadGuideline("Use_target_language_term_test.v1.gdl2"));
        List<DataInstance> result = interpreter.executeGuidelines(guidelineList, dataInstances);
        DataInstance dataInstance = result.get(0);
        DvCodedText dvCodedText = dataInstance.getDvCodedText(PATH);
        assertThat(dvCodedText.getValue(), is("Five"));
    }
}
