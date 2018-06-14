package org.gdl2.runtime;

import org.gdl2.datatypes.DvCodedText;
import org.gdl2.datatypes.DvDateTime;
import org.gdl2.datatypes.DvOrdinal;
import org.gdl2.datatypes.DvQuantity;
import org.gdl2.model.Guideline;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Goal;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertTrue;

public class UseTemplateExpressionTest extends TestCommon {
    private Guideline guideline;
    private Interpreter interpreter;
    private List<DataInstance> input;
    private List<DataInstance> output;


    @BeforeMethod
    public void setUp() throws Exception {
        input = new ArrayList<>();
        interpreter = new Interpreter();
    }

    @Test
    public void can_use_template_create_ordinal() throws Exception {
        List<Guideline> guidelines = loadSingleGuideline("use_template_with_ordinal_test.v0.1.gdl2");
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.get(0).getRoot(), instanceOf(DvOrdinal.class));
        DvOrdinal dvOrdinal = (DvOrdinal) output.get(0).get("/");
        assertThat(dvOrdinal.toString(), is("3|ATC::C10AA05|atorvastatin|"));
    }

    @Test
    public void can_use_template_create_quantity() throws Exception {
        guideline = loadGuideline("use_template_with_quantity_test.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.get(0).getRoot(), instanceOf(DvQuantity.class));
        DvQuantity dvQuantity = (DvQuantity) output.get(0).getRoot();
        assertThat(dvQuantity.toString(), is("7.5,mg"));
    }

    @Test
    public void can_use_template_create_linked_hash_map() throws Exception {
        guideline = loadGuideline("use_template_with_linked_hash_map_test.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.get(0).getRoot(), instanceOf(LinkedHashMap.class));
        LinkedHashMap linkedHashMap = (LinkedHashMap) output.get(0).getRoot();
        assertThat(linkedHashMap.get("unit"), is("mg"));
        assertThat(linkedHashMap.get("precision"), is(1.0));
        assertThat(linkedHashMap.get("magnitude"), is(2.0));
    }

    @Test
    public void can_use_template_create_linked_hash_map_value_set_by_previous_rule() throws Exception {
        guideline = loadGuideline("use_template_with_linked_hash_map_test3.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.get(0).getRoot(), instanceOf(LinkedHashMap.class));
        LinkedHashMap linkedHashMap = (LinkedHashMap) output.get(0).getRoot();
        assertThat(linkedHashMap.get("unit"), is("mg"));
        assertThat(linkedHashMap.get("precision"), is(1.0));
        assertThat(linkedHashMap.get("magnitude"), is(0.5));
    }

    @Test
    public void can_use_template_create_2_linked_hash_maps() throws Exception {
        guideline = loadGuideline("use_template_with_linked_hash_map_test2.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(2));
        assertThat(output.get(0).getRoot(), instanceOf(LinkedHashMap.class));
        LinkedHashMap linkedHashMap = (LinkedHashMap) output.get(0).getRoot();
        assertThat(linkedHashMap.get("unit"), is("mg"));
        assertThat(linkedHashMap.get("precision"), is(1.0));
        assertThat(linkedHashMap.get("magnitude"), is(0.5));

        assertThat(output.get(1).getRoot(), instanceOf(LinkedHashMap.class));
        linkedHashMap = (LinkedHashMap) output.get(1).getRoot();
        assertThat(linkedHashMap.get("unit"), is("mg"));
        assertThat(linkedHashMap.get("precision"), is(1.0));
        assertThat(linkedHashMap.get("magnitude"), is(2.4));
    }

    @Test
    public void can_use_template_create_quantity_with_double_variable() throws Exception {
        guideline = loadGuideline("use_template_with_quantity_set_value_test.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.get(0).getRoot(), instanceOf(DvQuantity.class));
        DvQuantity dvQuantity = (DvQuantity) output.get(0).getRoot();
        assertThat(dvQuantity.toString(), is("8.5,mg"));
    }

    @Test
    public void can_use_template_create_quantity_with_twice_each_with_different_double_variable() throws Exception {
        guideline = loadGuideline("use_template_with_quantity_twice_set_value_test.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.get(0).getRoot(), instanceOf(DvQuantity.class));
        DvQuantity dvQuantity = (DvQuantity) output.get(0).getRoot();
        assertThat(dvQuantity.toString(), is("3.5,mg")); // only the last set value counts
    }

    @Test
    public void can_use_template_create_quantity_with_calculated_double_variable() throws Exception {
        guideline = loadGuideline("use_template_with_quantity_set_calculated_value_test.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        input.add(new DataInstance.Builder()
                .modelId("org.hl7.fhir.dstu3.model.Observation")
                .addValue("/code/coding[0]", DvCodedText.valueOf("LOINC::13457-7|LDL Cholesterol|"))
                .addValue("/valueQuantity", DvQuantity.builder().magnitude(10.0).precision(1).unit("mmol/l").build())
                .addValue("/issued", new DvDateTime())
                .build());
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.get(0).getRoot(), instanceOf(DvQuantity.class));
        DvQuantity dvQuantity = (DvQuantity) output.get(0).getRoot();
        assertThat(dvQuantity.toString(), is("5.0,mg")); // only the last set value counts
    }

    @Test
    public void can_use_template_create_3_fhir_domain_resources() throws Exception {
        interpreter = buildInterpreterWithFhirPluginAndCurrentDateTime();
        guideline = loadGuideline("use_template_with_fhir_resources_test.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertTrue(output.get(0).getRoot() instanceof MedicationRequest);
        assertTrue(output.get(1).getRoot() instanceof Goal);
        assertTrue(output.get(2).getRoot() instanceof Appointment);
    }

    @Test
    public void can_use_template_create_fhir_appointment_with_datetime_variable() throws Exception {
        interpreter = buildInterpreterWithFhirPluginAndCurrentDateTime("2013-04-20T14:00:00");
        guideline = loadGuideline("use_template_fhir_appointment_set_datetime_test.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.get(0).getRoot(), instanceOf(Appointment.class));
        Appointment appointment = (Appointment) output.get(0).getRoot();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        assertThat(appointment.getRequestedPeriod().get(0).getStart(), is(dateFormat.parse("2013-04-20")));
        assertThat(appointment.getRequestedPeriod().get(0).getEnd(), is(dateFormat.parse("2013-04-25")));
    }

    @Test
    public void can_use_template_set_multiple_values_of_diff_types() throws Exception {
        interpreter = buildInterpreterWithFhirPluginAndCurrentDateTime("2013-04-20T14:00:00");
        guideline = loadGuideline("use_template_set_multiple_values_of_diff_types.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.get(0).getRoot(), instanceOf(Appointment.class));
        Appointment appointment = (Appointment) output.get(0).getRoot();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        assertThat(appointment.getRequestedPeriod().get(0).getStart(), is(dateFormat.parse("2013-04-20")));
        assertThat(appointment.getStatus().toCode(), is("proposed"));
    }

    @Test
    public void can_use_template_create_fhir_appointment_with_current_datetime_variable() throws Exception {
        interpreter = buildInterpreterWithFhirPluginAndCurrentDateTime("2013-04-20T14:00:00");
        guideline = loadGuideline("use_template_fhir_appointment_set_with_current_datetime.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.get(0).getRoot(), instanceOf(Appointment.class));
        Appointment appointment = (Appointment) output.get(0).getRoot();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        assertThat(dateFormat.format(appointment.getRequestedPeriod().get(0).getStart()), is("2013-04-20T14:00:00"));
    }

    @Test
    public void can_use_template_create_fhir_appointment_with_calculated_datetime_variable() throws Exception {
        interpreter = buildInterpreterWithFhirPluginAndCurrentDateTime("2013-04-20T14:00:00");
        guideline = loadGuideline("use_template_fhir_appointment_set_with_calculated_datetime.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.get(0).getRoot(), instanceOf(Appointment.class));
        Appointment appointment = (Appointment) output.get(0).getRoot();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        assertThat(dateFormat.format(appointment.getRequestedPeriod().get(0).getStart()), is("2013-04-20T14:00:00"));
        assertThat(dateFormat.format(appointment.getRequestedPeriod().get(0).getEnd()), is("2013-07-20T14:00:00"));
    }

    private Interpreter buildInterpreterWithFhirPluginAndCurrentDateTime(String datetime) {
        return new Interpreter(RuntimeConfiguration.builder()
                .currentDateTime(datetime == null ? new DvDateTime() : DvDateTime.valueOf(datetime))
                .objectCreatorPlugin(new FhirDstu3ResourceCreator())
                .build());
    }

    private Interpreter buildInterpreterWithFhirPluginAndCurrentDateTime() {
        return buildInterpreterWithFhirPluginAndCurrentDateTime(null);
    }
}