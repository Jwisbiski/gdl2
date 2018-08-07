package org.gdl2.runtime;

import org.gdl2.cdshooks.Card;
import org.gdl2.datatypes.DvDateTime;
import org.gdl2.datatypes.DvQuantity;
import org.gdl2.model.Guideline;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class CdsHooksGuidelinesTest extends TestCommon {
    private Interpreter interpreter;
    private Guideline guideline;
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
        assertThat(dvQuantity.getUnit(), is("m2"));
    }

    @Test
    public void can_use_same_template_create_two_fhir_instances_within_single_rule() throws Exception {
        interpreter = buildInterpreterWithFhirPluginAndCurrentDateTime();
        guideline = loadGuideline("use_template_with_fhir_resources_test2.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        List<Card> cards = interpreter.executeCdsHooksGuidelines(guidelines, Collections.emptyList());
        assertThat(cards.get(0).getSuggestions().get(0).getActions().size(), is(2));
        assertMedicationRequest(cards.get(0).getSuggestions().get(0).getActions().get(0).getResource(),
                "7mg once daily", 7);
        assertMedicationRequest(cards.get(0).getSuggestions().get(0).getActions().get(1).getResource(),
                "8mg once daily", 8);
    }

    private void assertMedicationRequest(Object object, String text, int value) throws Exception {
        assertThat(object, instanceOf(MedicationRequest.class));
        MedicationRequest medicationRequest = (MedicationRequest) object;
        assertThat(medicationRequest.getDosageInstruction().get(0).getText(), is(text));
        assertThat(medicationRequest.getDosageInstruction().get(0).getDoseSimpleQuantity().getValue(),
                is(BigDecimal.valueOf(value)));
    }

    private Interpreter buildInterpreterWithFhirPluginAndCurrentDateTime() {
        return new Interpreter(RuntimeConfiguration.builder()
                .currentDateTime(ZonedDateTime.now())
                .objectCreatorPlugin(new FhirDstu3ResourceCreator())
                .build());
    }
}
