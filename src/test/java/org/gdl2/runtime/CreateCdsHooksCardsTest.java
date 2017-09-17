package org.gdl2.runtime;

import org.gdl2.cdshooks.Action;
import org.gdl2.cdshooks.Card;
import org.gdl2.datatypes.DvCodedText;
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
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.testng.Assert.assertNotNull;

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
    public void can_create_cdshooks_card_with_referenced_links() throws Exception {
        guidelines = loadSingleGuideline("cdshooks_card_referenced_link_test.v0.1.gdl2");
        List<Card> cardList = interpreter.executeCdsHooksGuidelines(guidelines, input);
        assertThat(cardList.size(), is(1));
        Card card = cardList.get(0);
        assertThat(card.getLinks().get(0).getLabel(), is("NICE guideline"));
        assertThat(card.getLinks().get(0).getUrl().toString(), is("https://www.nice.org.uk/guidance/CG181"));
    }

    @Test
    public void can_create_cdshooks_card_without_side_effect() throws Exception {
        guidelines = loadSingleGuideline("cdshooks_card_single_suggestion_test.v0.1.gdl2");
        List<Card> cardList = interpreter.executeCdsHooksGuidelines(guidelines, input);
        assertThat(cardList.get(0).getSuggestions().get(0).getActions().size(), is(1));
        assertNotNull(guidelines.get(0).getDefinition().getRules().get("gt0034")
                .getCards().get(0).getSuggestions().get(0).getActions().get(0).getResourceTemplate());
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

    @Test
    public void can_create_cdshooks_card_dynamic_summary_detail() throws Exception {
        input.add(new DataInstance.Builder()
                .modelId("org.hl7.fhir.dstu3.model.MedicationStatement")
                .addValue("/medicationCodeableConcept/coding[0]", DvCodedText.valueOf("ATC::C10AA05|Statin|"))
                .build());
        guidelines = loadSingleGuideline("cdshooks_card_dynamic_summary_detail_test.v0.1.gdl2");
        List<Card> cardList = interpreter.executeCdsHooksGuidelines(guidelines, input);
        assertThat(cardList.size(), is(1));
        Card card = cardList.get(0);
        assertThat(card.getSummary(), is("card summary: Statin"));
        assertThat(card.getDetail(), is("card detail: ATC::C10AA05|Statin|"));
    }

    @Test
    public void can_create_multiple_cards_by_different_rules() throws Exception {
        guidelines = loadSingleGuideline("cdshooks_multiple_cards_test.v0.1.gdl2");
        List<Card> cardList = interpreter.executeCdsHooksGuidelines(guidelines, input);
        assertThat(cardList.size(), is(2));
        assertThat(cardList.get(0).getSummary(), is("card 1"));
        assertThat(cardList.get(1).getSummary(), is("card 2"));
    }

    private Interpreter buildInterpreterWithFhirPluginAndCurrentDateTime(String datetime) {
        return new Interpreter(
                RuntimeConfiguration.builder()
                        .currentDateTime(datetime == null ? new DvDateTime() : DvDateTime.valueOf(datetime))
                        .objectCreatorPlugin(new FhirDstu3ResourceCreator())
                        .build());
    }
}
