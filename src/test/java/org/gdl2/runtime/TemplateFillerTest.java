package org.gdl2.runtime;

import com.google.gson.Gson;
import org.gdl2.datatypes.DvDateTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.testng.Assert.assertEquals;

public class TemplateFillerTest extends TestCommon {
    private TemplateFiller templateFiller = new TemplateFiller();
    private String source;
    private Map<String, Object> localValues;
    private Map<String, List<Object>> globalValues;

    @BeforeMethod
    public void before_test() {
        localValues = new HashMap<>();
        globalValues = new HashMap<>();
    }

    @Test
    public void can_fill_single_integer_value_whole_string() {
        source = "{$gt2000}";
        localValues.put("gt2000", 80);
        assertThat(templateFiller.replaceVariablesWithValues(source, localValues, globalValues), is("80"));
    }

    @Test
    public void can_fill_single_integer_value_whole_string_using_global_values() {
        source = "{$gt2000}";
        globalValues.put("gt2000", Collections.singletonList(80));
        assertThat(templateFiller.replaceVariablesWithValues(source, localValues, globalValues), is("80"));
    }

    @Test
    public void can_fill_single_datetime_value_whole_string() {
        source = "{$gt2000}";
        localValues.put("gt2000", DvDateTime.valueOf("2017-04-14T13:55:55"));
        assertThat(templateFiller.replaceVariablesWithValues(source, localValues, globalValues), is("2017-04-14T13:55:55"));
    }

    @Test
    public void can_fill_single_string_value_whole_string() {
        source = "{$gt2000}";
        localValues.put("gt2000", "mg");
        assertThat(templateFiller.replaceVariablesWithValues(source, localValues, globalValues), is("mg"));
    }

    @Test
    public void can_fill_single_double_value_whole_string() {
        source = "{$gt2000}";
        localValues.put("gt2000", 8.5);
        assertThat(templateFiller.replaceVariablesWithValues(source, localValues, globalValues), is("8.5"));
    }

    @Test
    public void can_fill_single_integer_value_in_part_string() {
        source = "{$gt2000}mg daily";
        localValues.put("gt2000", 80);
        assertThat(templateFiller.replaceVariablesWithValues(source, localValues, globalValues), is("80mg daily"));
    }

    @Test
    public void can_fill_single_double_value_in_part_string() {
        source = "{$gt2000}mg daily";
        localValues.put("gt2000", 8.5);
        assertThat(templateFiller.replaceVariablesWithValues(source, localValues, globalValues), is("8.5mg daily"));
    }

    @Test
    public void can_fill_single_string_value_in_part_string() {
        source = "80{$gt2000} daily";
        localValues.put("gt2000", "mg");
        assertThat(templateFiller.replaceVariablesWithValues(source, localValues, globalValues), is("80mg daily"));
    }

    @Test
    public void can_fill_double_and_string_values() {
        source = "{$gt1000}{$gt2000} daily";
        localValues.put("gt1000", 80);
        localValues.put("gt2000", "mg");
        assertThat(templateFiller.replaceVariablesWithValues(source, localValues, globalValues), is("80mg daily"));
    }

    @Test
    public void can_traverse_map_fill_values() throws Exception {
        Map map = new Gson().fromJson(loadJson("medication_request_test"), Map.class);
        localValues.put("gt0000", "plan");
        localValues.put("gt0001", "http://www.whocc.no/atc");
        localValues.put("gt0002", "C10AA05");
        localValues.put("gt0003", "atorvastatin");
        localValues.put("gt0004", 1.0);
        localValues.put("gt0005", 7.0);
        localValues.put("gt0006", "mg");
        localValues.put("gt0007", 1.0);
        localValues.put("gt0008", 1.0);
        localValues.put("gt0009", "d");
        localValues.put("gt0010", "http://unitsofmeasure.org");
        templateFiller.traverseMapAndReplaceAllVariablesWithValues(map, localValues, globalValues);
        Map expected = new Gson().fromJson(loadJson("medication_request_test_expected"), Map.class);
        assertEquals(map, expected);
    }

    @Test
    public void can_traverse_map_fill_datetime_values() throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Map map = new Gson().fromJson(loadJson("appointment_test"), Map.class);
        localValues.put("gt2000", dateFormat.parse("2013-04-20"));
        localValues.put("gt2001", dateFormat.parse("2013-04-25"));
        templateFiller.traverseMapAndReplaceAllVariablesWithValues(map, localValues, globalValues);
        Map expected = new Gson().fromJson(loadJson("appointment_test_expected"), Map.class);
        assertEquals(map, expected);
    }
}
