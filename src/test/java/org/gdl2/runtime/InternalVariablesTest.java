package org.gdl2.runtime;

import org.gdl2.cdshooks.Card;
import org.gdl2.datatypes.DvCount;
import org.gdl2.model.Guideline;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class InternalVariablesTest extends TestCommon {

    private Interpreter interpreter;
    private List<DataInstance> input;
    private List<Guideline> guidelines;

    @BeforeMethod
    public void setUp() throws Exception {
        input = new ArrayList<>();
        input.add(new DataInstance.Builder()
                .modelId("openEHR-EHR-OBSERVATION.chadsvas_score.v1")
                .addValue("/data[at0002]/events[at0003]/data[at0001]/items[at0099]", DvCount.valueOf(2))
                .build());
        interpreter = new Interpreter();
    }

    @Test
    public void can_use_interval_value_of_ordinal_type() throws Exception {
        guidelines = loadSingleGuideline("internal_variable_ordinal_test.v2.0.0.gdl2");
        List<Card> cardList = interpreter.executeCdsHooksGuidelines(guidelines, input);
        assertThat(cardList.size(), is(1));
        assertThat(cardList.get(0).getSummary(), is("Yearly stroke risk: 3"));
        assertThat(cardList.get(0).getDetail(), is("3|local::at0007|3.2%|"));
    }

    @Test
    public void can_use_interval_value_of_coded_text_type() throws Exception {
        guidelines = loadSingleGuideline("internal_variable_coded_text_test.v2.0.0.gdl2");
        List<Card> cardList = interpreter.executeCdsHooksGuidelines(guidelines, input);
        assertThat(cardList.size(), is(1));
        assertThat(cardList.get(0).getSummary(), is("Yearly stroke risk: 3.2%"));
    }

    @Test
    public void can_use_interval_value_of_text_type() throws Exception {
        guidelines = loadSingleGuideline("internal_variable_text_test.v2.0.0.gdl2");
        List<Card> cardList = interpreter.executeCdsHooksGuidelines(guidelines, input);
        assertThat(cardList.size(), is(1));
        assertThat(cardList.get(0).getSummary(), is("Yearly stroke risk: THREE"));
    }

    @Test
    public void can_use_interval_value_of_count_type() throws Exception {
        guidelines = loadSingleGuideline("internal_variable_count_test.v2.0.0.gdl2");
        List<Card> cardList = interpreter.executeCdsHooksGuidelines(guidelines, input);
        assertThat(cardList.size(), is(1));
        assertThat(cardList.get(0).getSummary(), is("Yearly stroke risk: 3"));
    }

    @Test
    public void can_use_interval_value_of_quantity_type() throws Exception {
        guidelines = loadSingleGuideline("internal_variable_quantity_test.v2.0.0.gdl2");
        List<Card> cardList = interpreter.executeCdsHooksGuidelines(guidelines, input);
        assertThat(cardList.size(), is(1));
        assertThat(cardList.get(0).getSummary(), is("Yearly stroke risk: 3.2,%"));
    }
}
