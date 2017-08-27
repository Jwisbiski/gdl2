package org.gdl2.runtime;

import org.gdl2.cdshooks.Action;
import org.gdl2.cdshooks.Card;
import org.gdl2.datatypes.DvDateTime;
import org.gdl2.model.Guideline;
import org.hamcrest.Matchers;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Goal;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CreateCdsHooksCardsTest extends TestCommon {
    private Interpreter interpreter;
    private List<DataInstance> input;
    private List<Guideline> guidelines;

    @BeforeMethod
    public void setUp() throws Exception {
        input = new ArrayList<>();
        interpreter = buildInterpreterWithFhirPluginAndCurrentDateTime("2013-04-20T14:00:00");
    }

    @Test
    public void can_create_cdshooks_card_single_suggestion_with_specific_reference_and_appointment_dates() throws Exception {
        guidelines = loadSingleGuideline("cdshooks_card_single_suggestion_test.v0.1.gdl2");
        List<Card> cardList = interpreter.executeCdsHooksGuidelines(guidelines, input);
        assertThat(cardList.size(), is(1));
        Card card = cardList.get(0);
        assertThat(card.getSource().getLabel(), is("NICE guideline"));
        assertThat(card.getSource().getUrl().toString(), is("https://www.nice.org.uk/guidance/CG181"));
        assertThat(card.getSuggestions().size(), is(1));

        assertThat(card.getSuggestions().get(0).getActions().get(0).getType(), is(Action.ActionType.CREATE));
        Object object = card.getSuggestions().get(0).getActions().get(0).getResource();
        assertThat("Appointment expected", object instanceof Appointment);
        Appointment appointment = (Appointment) object;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        assertThat(appointment.getRequestedPeriod().get(0).getStart(), Matchers.is(dateFormat.parse("2013-04-20")));
        assertThat(appointment.getRequestedPeriod().get(0).getEnd(), Matchers.is(dateFormat.parse("2013-04-25")));
    }

    @Test
    public void can_create_cdshooks_card_three_suggestions() throws Exception {
        guidelines = loadSingleGuideline("cdshooks_card_three_suggestions_test.v0.1.gdl2");
        List<Card> cardList = interpreter.executeCdsHooksGuidelines(guidelines, input);
        assertThat(cardList.size(), is(1));
        Card card = cardList.get(0);
        assertThat(card.getSource().getLabel(), is("NICE guideline"));
        assertThat(card.getSource().getUrl().toString(), is("https://www.nice.org.uk/guidance/CG181"));
        assertThat(card.getSuggestions().size(), is(3));
        assertThat(card.getSuggestions().get(0).getActions().size(), is(1));
        assertThat("MedicationRequest expected", card.getSuggestions().get(0).getActions().get(0).getResource() instanceof MedicationRequest);
        assertThat(card.getSuggestions().get(1).getActions().size(), is(1));
        assertThat("Goal expected", card.getSuggestions().get(1).getActions().get(0).getResource() instanceof Goal);
        assertThat(card.getSuggestions().get(2).getActions().size(), is(1));
        assertThat("Appointment expected", card.getSuggestions().get(2).getActions().get(0).getResource() instanceof Appointment);
    }

    private Interpreter buildInterpreterWithFhirPluginAndCurrentDateTime(String datetime) {
        Map<String, Object> params = new HashMap<>();
        if (datetime != null) {
            params.put(Interpreter.CURRENT_DATETIME, DvDateTime.valueOf(datetime));
        }
        params.put(Interpreter.OBJECT_CREATOR, new FhirDstu3ResourceCreator());
        return new Interpreter(params);
    }
}
