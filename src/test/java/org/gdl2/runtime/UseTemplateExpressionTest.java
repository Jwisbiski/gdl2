package org.gdl2.runtime;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import org.gdl2.datatypes.*;
import org.gdl2.model.Guideline;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Goal;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
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
    private Gson gson = new Gson();
    private String json;

    @BeforeMethod
    public void setUp() {
        input = new ArrayList<>();
        interpreter = new Interpreter();
    }

    @Test
    public void can_use_template_create_ordinal() throws Exception {
        List<Guideline> guidelines = loadSingleGuideline("use_template_with_ordinal_test.v0.1.gdl2");
        output = interpreter.executeGuidelines(guidelines, input);
        DvOrdinal dvOrdinal = gson.fromJson(gson.toJson(output.get(0).getRoot()), DvOrdinal.class);
        assertThat(dvOrdinal.toString(), is("3|ATC::C10AA05|atorvastatin|"));
    }

    @Test
    public void can_use_template_create_boolean_value() throws Exception {
        List<Guideline> guidelines = loadSingleGuideline("use_template_with_boolean_test.v0.1.gdl2");
        output = interpreter.executeGuidelines(guidelines, input);
        String json = gson.toJson(output.get(0).getRoot());
        assertThat(JsonPath.read(json, "$.value"), is(true));
    }

    @Test
    public void can_use_template_with_last_value_of_variable() throws Exception {
        List<Guideline> guidelines = loadSingleGuideline("use_template_with_last_value_test.v0.1.gdl2");
        output = interpreter.executeGuidelines(guidelines, input);
        DvOrdinal dvOrdinal = gson.fromJson(gson.toJson(output.get(0).getRoot()), DvOrdinal.class);
        assertThat(dvOrdinal.getValue(), is(2));
    }

    @Test
    public void can_use_template_without_data_bindings() throws Exception {
        List<Guideline> guidelines = loadSingleGuideline("use_template_without_data_bindings.v0.1.gdl2");
        output = interpreter.executeGuidelines(guidelines, input);
        DvOrdinal dvOrdinal = gson.fromJson(gson.toJson(output.get(0).getRoot()), DvOrdinal.class);
        assertThat(dvOrdinal.toString(), is("3|ATC::C10AA05|atorvastatin|"));
    }

    @Test
    public void can_use_template_create_ordinal_with_element_bindings() throws Exception {
        List<Guideline> guidelines = loadSingleGuideline("use_template_with_ordinal_test2.v0.1.gdl2");
        output = interpreter.executeGuidelines(guidelines, input);
        DataInstance dataInstance = output.get(0);
        DvOrdinal dvOrdinal = gson.fromJson(gson.toJson(dataInstance.getRoot()), DvOrdinal.class);
        assertThat(dvOrdinal.toString(), is("3|ATC::C10AA05|atorvastatin|"));
        assertThat(dataInstance.getDvCodedText("/symbol").getDefiningCode().getTerminology(), is("ATC"));
        assertThat(dataInstance.getDvCodedText("/symbol").getDefiningCode().getCode(), is("C10AA05"));
        assertThat(dataInstance.getDvCodedText("/symbol").getValue(), is("atorvastatin"));
        assertThat(dataInstance.getDvText("/symbol/value").getValue(), is("atorvastatin"));
    }

    @Test
    public void can_use_template_and_select_output_with_element_bindings() throws Exception {
        List<Guideline> guidelines = new ArrayList<>();
        guidelines.add(loadGuideline("use_template_with_ordinal_test2.v0.1.gdl2"));
        guidelines.add(loadGuideline("use_template_with_ordinal_select_output.v0.1.gdl2"));
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(2));
        DataInstance dataInstance = output.get(1);
        DvOrdinal dvOrdinal = gson.fromJson(gson.toJson(dataInstance.getRoot()), DvOrdinal.class);
        assertThat(dvOrdinal.toString(), is("3|test::test|success|"));
    }

    @Test
    public void can_use_template_and_select_output_with_dv_text_element_bindings_and_use_in_another_template() throws Exception {
        List<Guideline> guidelines = new ArrayList<>();
        guidelines.add(loadGuideline("use_template_with_ordinal_test2.v0.1.gdl2"));
        guidelines.add(loadGuideline("use_template_with_ordinal_test2.v0.1.gdl2"));
        guidelines.add(loadGuideline("use_template_with_ordinal_select_and_use_again.v0.1.gdl2"));
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(3));
        DataInstance dataInstance = output.get(2);
        String json = gson.toJson(dataInstance.getRoot());
        assertThat(JsonPath.read(json, "$.list_value.length()"), is(2));
        assertThat(JsonPath.read(json, "$.list_value[0].symbol.value"), is("atorvastatin"));
        assertThat(JsonPath.read(json, "$.list_value[1].symbol.value"), is("atorvastatin"));
    }

    @Test
    public void can_use_template_and_select_output_with_dv_coded_text_element_bindings_and_use_in_another_template() throws Exception {
        List<Guideline> guidelines = new ArrayList<>();
        guidelines.add(loadGuideline("use_template_with_ordinal_test2.v0.1.gdl2"));
        guidelines.add(loadGuideline("use_template_with_ordinal_select_and_use_again.v0.1.gdl2"));
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(2));
        DataInstance dataInstance = output.get(1);
        String json = gson.toJson(dataInstance.getRoot());
        assertThat(JsonPath.read(json, "$.symbol.value"), is("atorvastatin"));
    }

    @Test
    public void can_use_template_and_select_output_with_quantity_element_bindings_and_use_in_another_template() throws Exception {
        List<Guideline> guidelines = new ArrayList<>();
        guidelines.add(loadGuideline("use_template_with_quantity_test2.v0.1.gdl2"));
        guidelines.add(loadGuideline("use_template_with_ordinal_select_and_use_again2.v0.1.gdl2"));
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(2));
        DataInstance dataInstance = output.get(1);
        String json = gson.toJson(dataInstance.getRoot());
        assertThat(JsonPath.read(json, "$.value"), is(7.5));
    }

    @Test
    public void can_use_template_and_insert_selected_objects_in_array_with_template_only_first_one_selected() throws Exception {
        List<Guideline> guidelines = new ArrayList<>();
        input.add(new DataInstance.Builder()
                .modelId("org.gdl2.datatypes.DvOrdinal")
                .addValue("/symbol/value", DvText.valueOf("object-1"))
                .addValue("/", DvOrdinal.valueOf("1|terminology::code|object-1|"))
                .build());
        guidelines.add(loadGuideline("use_template_with_selected_object.v0.1.gdl2"));
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(1));
        DataInstance dataInstance = output.get(0);
        String json = gson.toJson(dataInstance.getRoot());
        assertThat(JsonPath.read(json, "$.list_value.length()"), is(1));
        assertThat(JsonPath.read(json, "$.list_value[0].symbol.value"), is("object-1"));
    }

    @Test
    public void can_use_template_and_insert_selected_objects_in_array_with_template_only_second_one_selected() throws Exception {
        List<Guideline> guidelines = new ArrayList<>();
        input.add(new DataInstance.Builder()
                .modelId("org.gdl2.datatypes.DvOrdinal")
                .addValue("/symbol/value", DvText.valueOf("object-2"))
                .addValue("/", DvOrdinal.valueOf("1|terminology::code|object-2|"))
                .build());
        guidelines.add(loadGuideline("use_template_with_selected_object.v0.1.gdl2"));
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(1));
        DataInstance dataInstance = output.get(0);
        String json = gson.toJson(dataInstance.getRoot());
        assertThat(JsonPath.read(json, "$.list_value.length()"), is(1));
        assertThat(JsonPath.read(json, "$.list_value[0].symbol.value"), is("object-2"));
    }

    @Test
    public void can_use_template_and_insert_selected_objects_in_array_with_template_only_third_one_selected() throws Exception {
        List<Guideline> guidelines = new ArrayList<>();
        input.add(new DataInstance.Builder()
                .modelId("org.gdl2.datatypes.DvOrdinal")
                .addValue("/symbol/value", DvText.valueOf("object-3"))
                .addValue("/", DvOrdinal.valueOf("1|terminology::code|object-3|"))
                .build());
        guidelines.add(loadGuideline("use_template_with_selected_object.v0.1.gdl2"));
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(1));
        DataInstance dataInstance = output.get(0);
        String json = gson.toJson(dataInstance.getRoot());
        assertThat(JsonPath.read(json, "$.list_value.length()"), is(1));
        assertThat(JsonPath.read(json, "$.list_value[0].symbol.value"), is("object-3"));
    }

    @Test
    public void can_use_template_and_insert_selected_objects_in_array_with_template_three_one_each_selected() throws Exception {
        List<Guideline> guidelines = new ArrayList<>();
        input.add(new DataInstance.Builder()
                .modelId("org.gdl2.datatypes.DvOrdinal")
                .addValue("/symbol/value", DvText.valueOf("object-1"))
                .addValue("/", DvOrdinal.valueOf("1|terminology::code|object-1|"))
                .build());
        input.add(new DataInstance.Builder()
                .modelId("org.gdl2.datatypes.DvOrdinal")
                .addValue("/symbol/value", DvText.valueOf("object-2"))
                .addValue("/", DvOrdinal.valueOf("1|terminology::code|object-2|"))
                .build());
        input.add(new DataInstance.Builder()
                .modelId("org.gdl2.datatypes.DvOrdinal")
                .addValue("/symbol/value", DvText.valueOf("object-3"))
                .addValue("/", DvOrdinal.valueOf("1|terminology::code|object-3|"))
                .build());
        guidelines.add(loadGuideline("use_template_with_selected_object.v0.1.gdl2"));
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(1));
        DataInstance dataInstance = output.get(0);
        String json = gson.toJson(dataInstance.getRoot());
        assertThat(JsonPath.read(json, "$.list_value.length()"), is(3));
        assertThat(JsonPath.read(json, "$.list_value[0].symbol.value"), is("object-1"));
        assertThat(JsonPath.read(json, "$.list_value[1].symbol.value"), is("object-2"));
        assertThat(JsonPath.read(json, "$.list_value[2].symbol.value"), is("object-3"));
    }

    @Test
    public void can_use_template_and_insert_selected_objects_in_array_with_template_without_all_keyword() throws Exception {
        List<Guideline> guidelines = new ArrayList<>();
        input.add(new DataInstance.Builder()
                .modelId("org.gdl2.datatypes.DvOrdinal")
                .addValue("/symbol/value", DvText.valueOf("object-1"))
                .addValue("/", DvOrdinal.valueOf("1|terminology::code|object-1|"))
                .build());
        input.add(new DataInstance.Builder()
                .modelId("org.gdl2.datatypes.DvOrdinal")
                .addValue("/symbol/value", DvText.valueOf("object-2"))
                .addValue("/", DvOrdinal.valueOf("1|terminology::code|object-2|"))
                .build());
        input.add(new DataInstance.Builder()
                .modelId("org.gdl2.datatypes.DvOrdinal")
                .addValue("/symbol/value", DvText.valueOf("object-3"))
                .addValue("/", DvOrdinal.valueOf("1|terminology::code|object-3|"))
                .build());
        guidelines.add(loadGuideline("use_template_with_selected_object_without_all_keyword.v0.1.gdl2"));
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(1));
        DataInstance dataInstance = output.get(0);
        String json = gson.toJson(dataInstance.getRoot());
        assertThat(JsonPath.read(json, "$.list_value.length()"), is(3));
        assertThat(JsonPath.read(json, "$.list_value[0].symbol.value"), is("object-1"));
        assertThat(JsonPath.read(json, "$.list_value[1].symbol.value"), is("object-2"));
        assertThat(JsonPath.read(json, "$.list_value[2].symbol.value"), is("object-3"));
    }

    @Test
    public void can_use_template_and_insert_selected_objects_in_array_with_template_mixed_all_keyword() throws Exception {
        List<Guideline> guidelines = new ArrayList<>();
        input.add(new DataInstance.Builder()
                .modelId("org.gdl2.datatypes.DvOrdinal")
                .addValue("/symbol/value", DvText.valueOf("object-1"))
                .addValue("/", DvOrdinal.valueOf("1|terminology::code|object-1|"))
                .build());
        input.add(new DataInstance.Builder()
                .modelId("org.gdl2.datatypes.DvOrdinal")
                .addValue("/symbol/value", DvText.valueOf("object-2"))
                .addValue("/", DvOrdinal.valueOf("1|terminology::code|object-2|"))
                .build());
        input.add(new DataInstance.Builder()
                .modelId("org.gdl2.datatypes.DvOrdinal")
                .addValue("/symbol/value", DvText.valueOf("object-3"))
                .addValue("/", DvOrdinal.valueOf("1|terminology::code|object-3|"))
                .build());
        guidelines.add(loadGuideline("use_template_with_selected_object_mixed_all_keyword.v0.1.gdl2"));
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(1));
        DataInstance dataInstance = output.get(0);
        String json = gson.toJson(dataInstance.getRoot());
        assertThat(JsonPath.read(json, "$.list_value.length()"), is(3));
        assertThat(JsonPath.read(json, "$.list_value[0].symbol.value"), is("object-1"));
        assertThat(JsonPath.read(json, "$.list_value[1].symbol.value"), is("object-2"));
        assertThat(JsonPath.read(json, "$.list_value[2].symbol.value"), is("object-3"));
    }

    @Test
    public void can_use_template_and_insert_selected_objects_in_array_with_template_mixed_all_keyword2() throws Exception {
        List<Guideline> guidelines = new ArrayList<>();
        input.add(new DataInstance.Builder()
                .modelId("org.gdl2.datatypes.DvOrdinal")
                .addValue("/symbol/value", DvText.valueOf("object-1"))
                .addValue("/", DvOrdinal.valueOf("1|terminology::code|object-1|"))
                .build());
        input.add(new DataInstance.Builder()
                .modelId("org.gdl2.datatypes.DvOrdinal")
                .addValue("/symbol/value", DvText.valueOf("object-2"))
                .addValue("/", DvOrdinal.valueOf("1|terminology::code|object-2|"))
                .build());
        input.add(new DataInstance.Builder()
                .modelId("org.gdl2.datatypes.DvOrdinal")
                .addValue("/symbol/value", DvText.valueOf("object-3"))
                .addValue("/", DvOrdinal.valueOf("1|terminology::code|object-3|"))
                .build());
        guidelines.add(loadGuideline("use_template_with_selected_object_mixed_all_keyword2.v0.1.gdl2"));
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(1));
        DataInstance dataInstance = output.get(0);
        String json = gson.toJson(dataInstance.getRoot());
        assertThat(JsonPath.read(json, "$.list_value.length()"), is(3));
        assertThat(JsonPath.read(json, "$.list_value[0].symbol.value"), is("object-1"));
        assertThat(JsonPath.read(json, "$.list_value[1].symbol.value"), is("object-2"));
        assertThat(JsonPath.read(json, "$.list_value[2].symbol.value"), is("object-3"));
    }

    @Test
    public void can_use_template_and_insert_selected_objects_in_array_with_template_first_third_selected() throws Exception {
        List<Guideline> guidelines = new ArrayList<>();
        input.add(new DataInstance.Builder()
                .modelId("org.gdl2.datatypes.DvOrdinal")
                .addValue("/symbol/value", DvText.valueOf("object-1"))
                .addValue("/", DvOrdinal.valueOf("1|terminology::code|object-1|"))
                .build());
        input.add(new DataInstance.Builder()
                .modelId("org.gdl2.datatypes.DvOrdinal")
                .addValue("/symbol/value", DvText.valueOf("object-3"))
                .addValue("/", DvOrdinal.valueOf("1|terminology::code|object-3|"))
                .build());
        guidelines.add(loadGuideline("use_template_with_selected_object.v0.1.gdl2"));
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(1));
        DataInstance dataInstance = output.get(0);
        String json = gson.toJson(dataInstance.getRoot());
        assertThat(JsonPath.read(json, "$.list_value.length()"), is(2));
        assertThat(JsonPath.read(json, "$.list_value[0].symbol.value"), is("object-1"));
        assertThat(JsonPath.read(json, "$.list_value[1].symbol.value"), is("object-3"));
    }

    @Test
    public void can_use_template_and_insert_selected_objects_in_array_with_template_none_selected() throws Exception {
        List<Guideline> guidelines = new ArrayList<>();
        guidelines.add(loadGuideline("use_template_with_selected_object.v0.1.gdl2"));
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(1));
        DataInstance dataInstance = output.get(0);
        String json = gson.toJson(dataInstance.getRoot());
        assertThat(JsonPath.read(json, "$.list_value.length()"), is(0));
    }

    @Test
    public void can_use_template_create_quantity() throws Exception {
        guideline = loadGuideline("use_template_with_quantity_test.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        DvQuantity dvQuantity = gson.fromJson(gson.toJson(output.get(0).getRoot()), DvQuantity.class);
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
    public void can_use_template_create_linked_hash_map_value_set_by_the_previous_rule() throws Exception {
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
    public void can_use_template_create_linked_hash_map_value_set_by_the_same_rule() throws Exception {
        guideline = loadGuideline("use_template_with_linked_hash_map_test5.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.get(0).getRoot(), instanceOf(LinkedHashMap.class));
        LinkedHashMap linkedHashMap = (LinkedHashMap) output.get(0).getRoot();
        assertThat(linkedHashMap.get("unit"), is("mg"));
        assertThat(linkedHashMap.get("precision"), is(1.0));
        assertThat(linkedHashMap.get("magnitude"), is(0.5));
    }

    @Test
    public void can_use_template_create_with_multiple_input_variables() throws Exception {
        guideline = loadGuideline("use_template_with_linked_hash_map_test6.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        input.add(new DataInstance.Builder()
                .modelId("a_model")
                .addValue("/path_1", DvCount.valueOf(3))
                .addValue("/path_2", DvCount.valueOf(5))
                .build());
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(2));
        assertThat(output.get(0).getRoot(), instanceOf(LinkedHashMap.class));
        LinkedHashMap linkedHashMap = (LinkedHashMap) output.get(0).getRoot();
        assertThat(linkedHashMap.get("magnitude"), is(3.0));
        linkedHashMap = (LinkedHashMap) output.get(1).getRoot();
        assertThat(linkedHashMap.get("magnitude"), is(5.0));
    }

    @Test
    public void can_use_template_create_with_multiple_input_variables_with_missing_value() throws Exception {
        guideline = loadGuideline("use_template_with_linked_hash_map_test6.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        input.add(new DataInstance.Builder()
                .modelId("a_model")
                .addValue("/path_1", DvCount.valueOf(3))
                .build());
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(1));
        assertThat(output.get(0).getRoot(), instanceOf(LinkedHashMap.class));
        LinkedHashMap linkedHashMap = (LinkedHashMap) output.get(0).getRoot();
        assertThat(linkedHashMap.get("magnitude"), is(3.0));
    }

    @Test
    public void can_use_template_create_linked_hash_map_value_with_number_as_string() throws Exception {
        guideline = loadGuideline("use_template_with_linked_hash_map_test4.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.get(0).getRoot(), instanceOf(LinkedHashMap.class));
        LinkedHashMap linkedHashMap = (LinkedHashMap) output.get(0).getRoot();
        assertThat(linkedHashMap.get("unit"), is("mg"));
        assertThat(linkedHashMap.get("precision"), is("1"));
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
        DvQuantity dvQuantity = gson.fromJson(gson.toJson(output.get(0).getRoot()), DvQuantity.class);
        assertThat(dvQuantity.toString(), is("8.5,mg"));
    }

    @Test
    public void can_use_template_create_quantity_with_twice_each_with_different_double_variable() throws Exception {
        guideline = loadGuideline("use_template_with_quantity_twice_set_value_test.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertThat(output.size(), is(2));
        DvQuantity dvQuantity = gson.fromJson(gson.toJson(output.get(0).getRoot()), DvQuantity.class);
        assertThat(dvQuantity.toString(), is("8.5,mg"));
        dvQuantity = gson.fromJson(gson.toJson(output.get(1).getRoot()), DvQuantity.class);
        assertThat(dvQuantity.toString(), is("3.5,mg"));
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
        DvQuantity dvQuantity = gson.fromJson(gson.toJson(output.get(0).getRoot()), DvQuantity.class);
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
        interpreter = buildInterpreterWithFhirPluginAndCurrentDateTime("2013-04-20T14:00:00Z");
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
        interpreter = new Interpreter(ZonedDateTime.parse("2013-04-20T14:00:00Z"));
        guideline = loadGuideline("use_template_set_multiple_values_of_diff_types.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        String json = gson.toJson(output.get(0).getRoot());
        assertThat(JsonPath.read(json, "$.requestedPeriod[0].start"), is("2013-04-20"));
        assertThat(JsonPath.read(json, "$.status"), is("proposed"));
    }

    @Test
    public void can_use_template_create_fhir_appointment_with_current_datetime_variable() throws Exception {
        interpreter = new Interpreter(ZonedDateTime.parse("2013-04-20T14:00:00Z"));
        guideline = loadGuideline("use_template_fhir_appointment_set_with_current_datetime.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertZonedDateTime("$.requestedPeriod[0].start", "2013-04-20T14:00:00Z");
    }

    @Test
    public void can_use_template_create_fhir_appointment_with_current_datetime_variable_directly_in_template() throws Exception {
        interpreter = new Interpreter(ZonedDateTime.parse("2013-04-20T14:00:00Z"));
        guideline = loadGuideline("use_template_fhir_appointment_set_with_current_datetime2.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertZonedDateTime("$.requestedPeriod[0].start", "2013-04-20T14:00:00Z");
    }

    @Test
    public void can_use_template_create_fhir_appointment_with_current_datetime_variable_directly_in_generic_template() throws Exception {
        interpreter = new Interpreter(ZonedDateTime.parse("2013-04-20T14:00:00Z"));
        guideline = loadGuideline("use_template_fhir_appointment_set_with_current_datetime3.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertZonedDateTime("$.requestedPeriod[0].start", "2013-04-20T14:00Z");
    }

    @Test
    public void can_use_template_create_fhir_appointment_with_calculated_datetime_variable() throws Exception {
        interpreter = new Interpreter(ZonedDateTime.parse("2013-04-20T14:00:00+01:00"));
        guideline = loadGuideline("use_template_fhir_appointment_set_with_calculated_datetime.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertZonedDateTime("$.requestedPeriod[0].start", "2013-04-20T14:00:00+01:00");
        assertZonedDateTime("$.requestedPeriod[0].end", "2013-07-20T14:00:00+01:00");
    }

    @Test
    public void can_use_template_with_calculated_datetime_variable_as_iso_string() throws Exception {
        interpreter = new Interpreter(ZonedDateTime.parse("2013-04-20T14:00:00+02:00[Europe/Paris]"));
        guideline = loadGuideline("use_template_fhir_appointment_set_with_calculated_datetime.v0.1.gdl2");
        List<Guideline> guidelines = Collections.singletonList(guideline);
        output = interpreter.executeGuidelines(guidelines, input);
        assertZonedDateTime("$.requestedPeriod[0].start", "2013-04-20T14:00:00+02:00");
        assertZonedDateTime("$.requestedPeriod[0].end", "2013-07-20T14:00:00+02:00");
    }

    private Interpreter buildInterpreterWithFhirPluginAndCurrentDateTime(String datetime) {
        return new Interpreter(RuntimeConfiguration.builder()
                .currentDateTime(datetime == null ? ZonedDateTime.now() : ZonedDateTime.parse(datetime))
                .objectCreatorPlugin(new FhirDstu3ResourceCreator())
                .build());
    }

    private void assertZonedDateTime(String path, String expected) {
        json = gson.toJson(output.get(0).getRoot());
        assertThat(ZonedDateTime.parse(JsonPath.read(json, path)), is(ZonedDateTime.parse(expected)));
    }

    private Interpreter buildInterpreterWithFhirPluginAndCurrentDateTime() {
        return buildInterpreterWithFhirPluginAndCurrentDateTime(null);
    }
}