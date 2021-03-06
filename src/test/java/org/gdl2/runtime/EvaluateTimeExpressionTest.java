package org.gdl2.runtime;

import org.gdl2.datatypes.DvQuantity;
import org.gdl2.expression.ExpressionItem;
import org.gdl2.expression.Variable;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class EvaluateTimeExpressionTest extends TestCommon {
    private ExpressionItem expressionItem;
    private Interpreter interpreter;
    private Map<String, List<Object>> inputMap = new HashMap<>();
    private Object value;

    @BeforeMethod
    public void setUp() {
        interpreter = new Interpreter();
        inputMap.clear();
    }

    @Test
    public void can_evaluate_current_datetime_using_given_formatter_pattern() {
        expressionItem = parseExpression("$currentDateTime.string");
        interpreter = new Interpreter(
                RuntimeConfiguration.builder()
                        .currentDateTime(ZonedDateTime.parse("2018-05-29T17:12:19Z"))
                        .dateTimeFormatPattern("yyyy-MM-dd HH:mm")
                        .build());
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is("2018-05-29 17:12"));
    }

    @Test
    public void can_evaluate_current_datetime_using_zone_id() {
        ZoneId zoneId = ZoneId.of("Atlantic/Bermuda");
        expressionItem = parseExpression("$currentDateTime");
        interpreter = new Interpreter(
                RuntimeConfiguration.builder()
                        .timezoneId(zoneId)
                        .build());
        ZonedDateTime zonedDateTime = (ZonedDateTime) interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(zonedDateTime.getZone(), is(zoneId));
    }

    @Test
    public void can_evaluate_current_datetime_using_default_zone_UTC() {
        expressionItem = parseExpression("$currentDateTime");
        interpreter = new Interpreter();
        ZonedDateTime zonedDateTime = (ZonedDateTime) interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(zonedDateTime.getZone(), is(ZoneId.of("UTC")));
    }

    @Test
    public void can_evaluate_java_date_using_given_formatter_pattern() throws ParseException {
        expressionItem = parseExpression("$gt0100.string");
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-DD'T'HH:mm:ss");
        inputMap.put("gt0100", asList(sdf.parse("1952-01-10T00:00:00")));
        interpreter = new Interpreter(
                RuntimeConfiguration.builder()
                        .dateTimeFormatPattern("yyyy-MM-dd'T'HH:mm:ss")
                        .build());
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is("1952-01-10T00:00:00"));
    }

    @Test
    public void can_evaluate_variable_with_set_currentDateTime_value() {
        Variable variable = Variable.createByCode(Interpreter.CURRENT_DATETIME);
        interpreter = new Interpreter(ZonedDateTime.parse("2000-01-01T00:00:00Z"));
        value = interpreter.evaluateExpressionItem(variable, inputMap);
        assertThat(((ZonedDateTime) value).getYear(), is(2000));
    }

    @Test
    public void can_evaluate_current_datetime_year() {
        Variable variable = new Variable(Interpreter.CURRENT_DATETIME, null, null, "year");
        interpreter = new Interpreter(ZonedDateTime.parse("2000-01-01T00:00:00Z"));
        value = interpreter.evaluateExpressionItem(variable, inputMap);
        assertThat(value, is(2000));
    }

    @Test
    public void can_evaluate_any_datetime_year() {
        Variable variable = new Variable("gt0100", null, null, "year");
        interpreter = new Interpreter();
        inputMap.put("gt0100", asList(ZonedDateTime.parse("1952-01-10T00:00:00Z")));
        value = interpreter.evaluateExpressionItem(variable, inputMap);
        assertThat(value, is(1952));
    }

    @Test
    public void can_evaluate_any_date_year() {
        Variable variable = new Variable("gt0101", null, null, "year");
        interpreter = new Interpreter();
        inputMap.put("gt0101", asList(ZonedDateTime.parse("1952-01-10T00:00:00Z")));
        value = interpreter.evaluateExpressionItem(variable, inputMap);
        assertThat(value, is(1952));
    }

    @Test
    public void can_compare_datetime_and_years_expect_false() {
        expressionItem = parseExpression("$gt0113<=($currentDateTime-65,a)");
        inputMap.put("gt0113", asList(ZonedDateTime.parse("1952-01-10T00:00:00Z")));
        interpreter = new Interpreter(ZonedDateTime.parse("2017-01-09T23:59:59Z"));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    @Test
    public void can_evaluate_expression_with_zoned_datetime_and_local_date_plus_period_expect_true() {
        expressionItem = parseExpression("$gt1014<($gt1009+60,a)");
        inputMap.put("gt1009", asList(LocalDate.parse("1957-01-01")));
        inputMap.put("gt1014", asList(ZonedDateTime.parse("2016-01-01T00:00:00+01:00")));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_evaluate_expression_with_zoned_datetime_and_local_date_plus_period_expect_true_local_date_on_right() {
        expressionItem = parseExpression("$gt1014<(60,a+$gt1009)");
        inputMap.put("gt1009", asList(LocalDate.parse("1957-01-01")));
        inputMap.put("gt1014", asList(ZonedDateTime.parse("2016-01-01T00:00:00+01:00")));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_evaluate_expression_with_zoned_datetime_and_local_date_plus_period_expect_false() {
        expressionItem = parseExpression("$gt1014<($gt1009+60,a)");
        inputMap.put("gt1009", asList(LocalDate.parse("1957-01-01")));
        inputMap.put("gt1014", asList(ZonedDateTime.parse("2017-01-02T00:00:00+01:00")));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    @Test
    public void can_evaluate_expression_with_zoned_datetime_and_local_date_plus_period_expect_false_local_date_on_right() {
        expressionItem = parseExpression("$gt1014<(60,a + $gt1009)");
        inputMap.put("gt1009", asList(LocalDate.parse("1957-01-01")));
        inputMap.put("gt1014", asList(ZonedDateTime.parse("2017-01-02T00:00:00+01:00")));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    @Test
    public void can_evaluate_expression_local_date_with_zoned_datetime_expect_true() {
        expressionItem = parseExpression("$gt1014 > $gt1009");
        inputMap.put("gt1014", asList(LocalDate.parse("2017-01-02")));
        inputMap.put("gt1009", asList(ZonedDateTime.parse("2017-01-01T00:00:00+01:00")));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_evaluate_expression_local_date_with_zoned_datetime_expect_false() {
        expressionItem = parseExpression("$gt1014 > $gt1009");
        inputMap.put("gt1014", asList(LocalDate.parse("2017-01-01")));
        inputMap.put("gt1009", asList(ZonedDateTime.parse("2017-01-02T00:00:00+01:00")));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    @Test
    public void can_compute_datetime_variables_with_years() {
        expressionItem = parseExpression("$currentDateTime.year-$gt0008.year");
        inputMap.put("gt0008", asList(ZonedDateTime.parse("1960-01-10T00:00:00Z")));
        interpreter = new Interpreter(ZonedDateTime.parse("2000-01-09T23:59:59Z"));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(40.0));
    }

    @Test
    public void can_compare_datetime_and_years_expect_true() {
        expressionItem = parseExpression("$gt0113<=($currentDateTime-65,a)");
        inputMap.put("gt0113", asList(ZonedDateTime.parse("1952-01-10T00:00:00Z")));
        interpreter = new Interpreter(ZonedDateTime.parse("2017-01-10T00:00:01Z"));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_compare_datetime_and_months_expect_false() {
        expressionItem = parseExpression("$gt0113<=($currentDateTime-3,mo)");
        inputMap.put("gt0113", asList(ZonedDateTime.parse("2017-01-10T00:00:00Z")));
        interpreter = new Interpreter(ZonedDateTime.parse("2017-04-09T23:59:59Z"));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    @Test
    public void can_compare_datetime_and_months_expect_true() {
        expressionItem = parseExpression("$gt0113<=($currentDateTime-3,mo)");
        inputMap.put("gt0113", asList(ZonedDateTime.parse("2017-01-01T00:00:00Z")));
        interpreter = new Interpreter(ZonedDateTime.parse("2017-04-01T00:00:01Z"));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_compare_datetime_and_days_expect_false() {
        expressionItem = parseExpression("$gt0113<=($currentDateTime-14,d)");
        inputMap.put("gt0113", asList(ZonedDateTime.parse("2017-01-16T00:00:00Z")));
        interpreter = new Interpreter(ZonedDateTime.parse("2017-01-29T23:59:59Z"));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    @Test
    public void can_compare_datetime_and_days_expect_true() {
        expressionItem = parseExpression("$gt0113<=($currentDateTime-14,d)");
        inputMap.put("gt0113", asList(ZonedDateTime.parse("2017-01-16T00:00:00Z")));
        interpreter = new Interpreter(ZonedDateTime.parse("2017-01-30T00:00:01Z"));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_compare_datetime_and_2_hours_expect_true() {
        expressionItem = parseExpression("$gt0113<($currentDateTime-2,h)");
        inputMap.put("gt0113", asList(ZonedDateTime.parse("2017-01-16T00:00:00Z")));
        interpreter = new Interpreter(ZonedDateTime.parse("2017-01-16T02:00:01Z"));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_compare_datetime_and_2_hours_expect_false() {
        expressionItem = parseExpression("$gt0113<($currentDateTime-2,h)");
        inputMap.put("gt0113", asList(ZonedDateTime.parse("2017-01-16T00:00:00Z")));
        interpreter = new Interpreter(ZonedDateTime.parse("2017-01-16T01:59:59Z"));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    @Test
    public void can_evaluate_condition_with_two_dates_within_90_days_apart_expect_true() {
        expressionItem = parseExpression("$gt0023<($gt0004+90,d)");
        inputMap.put("gt0023", asList(ZonedDateTime.parse("2014-04-10T18:18:00Z")));
        inputMap.put("gt0004", asList(ZonedDateTime.parse("2014-01-11T13:14:11Z")));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_evaluate_greater_or_equal_than_with_dv_quantity_in_number_of_years_expect_true() {
        expressionItem = parseExpression("$gt0005>=10,a");
        inputMap.put("gt0005", asList(new DvQuantity("a", 20, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_evaluate_greater_than_with_dv_quantity_in_number_of_years_expect_true() {
        expressionItem = parseExpression("$gt0005>10,a");
        inputMap.put("gt0005", asList(new DvQuantity("a", 20, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_evaluate_less_or_equal_than_with_dv_quantity_in_number_of_years_expect_true() {
        expressionItem = parseExpression("$gt0005<=10,a");
        inputMap.put("gt0005", asList(new DvQuantity("a", 9, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_evaluate_less_than_with_dv_quantity_in_number_of_years_expect_true() {
        expressionItem = parseExpression("$gt0005<10,a");
        inputMap.put("gt0005", asList(new DvQuantity("a", 9, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_evaluate_greater_or_equal_than_with_dv_quantity_in_number_of_years_expect_false() {
        expressionItem = parseExpression("$gt0005>=10,a");
        inputMap.put("gt0005", asList(new DvQuantity("a", 5, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    @Test
    public void can_evaluate_greater_than_with_dv_quantity_in_number_of_years_expect_false() {
        expressionItem = parseExpression("$gt0005>10,a");
        inputMap.put("gt0005", asList(new DvQuantity("a", 5, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    @Test
    public void can_evaluate_less_or_equal_than_with_dv_quantity_in_number_of_years_expect_false() {
        expressionItem = parseExpression("$gt0005<=10,a");
        inputMap.put("gt0005", asList(new DvQuantity("a", 12, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    @Test
    public void can_evaluate_less_than_with_dv_quantity_in_number_of_years_expect_false() {
        expressionItem = parseExpression("$gt0005<10,a");
        inputMap.put("gt0005", asList(new DvQuantity("a", 12, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    @Test
    public void can_evaluate_expression_with_zoned_datetime_and_local_date_in_age_calculation() {
        interpreter = new Interpreter(ZonedDateTime.parse("2018-08-08T10:40:30+01:00"));
        expressionItem = parseExpression("(floor((($currentDateTime-$gt0001)/1,a)))");
        inputMap.put("gt0001", asList(LocalDate.parse("1972-10-30")));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(45.0));
    }

    @Test
    public void can_evaluate_zoned_datetime_as_iso_8601_string() {
        String isoString = "2016-04-10T01:00:00-04:00";
        expressionItem = parseExpression("$gt0005.string");
        inputMap.put("gt0005", asList(ZonedDateTime.parse(isoString)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(isoString));
    }

    @Test
    public void can_evaluate_zoned_datetime_as_iso_8601_string_with_datetime_pattern() {
        String isoString = "2016-04-10T01:00:00-04:00";
        expressionItem = parseExpression("$gt0005.string");
        inputMap.put("gt0005", asList(ZonedDateTime.parse(isoString)));
        interpreter = new Interpreter(RuntimeConfiguration.builder().dateTimeFormatPattern("yyyy-MM-dd HH:mm").build());
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is("2016-04-10 01:00"));
    }

    @Test
    public void can_evaluate_greater_than_with_two_dv_quantity_with_days_years_expect_true() {
        expressionItem = parseExpression("$gt0025>30,d");
        inputMap.put("gt0025", asList(new DvQuantity("a", 30, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_evaluate_less_than_with_two_dv_quantity_with_days_years_expect_false() {
        expressionItem = parseExpression("$gt0025<30,d");
        inputMap.put("gt0025", asList(new DvQuantity("a", 30, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    @Test
    public void can_evaluate_greater_than_with_two_dv_quantity_with_months_years_expect_true() {
        expressionItem = parseExpression("$gt0025>30,mo");
        inputMap.put("gt0025", asList(new DvQuantity("a", 30, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_evaluate_less_than_with_two_dv_quantity_with_months_years_expect_false() {
        expressionItem = parseExpression("$gt0025<30,mo");
        inputMap.put("gt0025", asList(new DvQuantity("a", 30, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    @Test
    public void can_evaluate_greater_than_with_two_dv_quantity_with_months_days_expect_false() {
        expressionItem = parseExpression("$gt0025>30,mo");
        inputMap.put("gt0025", asList(new DvQuantity("d", 30, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    @Test
    public void can_evaluate_less_than_with_two_dv_quantity_with_months_days_expect_true() {
        expressionItem = parseExpression("$gt0025<30,mo");
        inputMap.put("gt0025", asList(new DvQuantity("d", 30, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_evaluate_less_than_with_27_days_and_1_month() {
        expressionItem = parseExpression("$gt0025<1,mo");
        inputMap.put("gt0025", asList(new DvQuantity("d", 27, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_evaluate_6_days_less_than_1_week_expect_true() {
        expressionItem = parseExpression("$gt0025<1,wk");
        inputMap.put("gt0025", asList(new DvQuantity("d", 6, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }

    @Test
    public void can_evaluate_6_days_greater_than_1_week_expect_false() {
        expressionItem = parseExpression("$gt0025>1,wk");
        inputMap.put("gt0025", asList(new DvQuantity("d", 6, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(false));
    }

    @Test
    public void can_evaluate_7_days_equal_to_1_week_expect_true() {
        expressionItem = parseExpression("$gt0025==1,wk");
        inputMap.put("gt0025", asList(new DvQuantity("d", 7, 0)));
        value = interpreter.evaluateExpressionItem(expressionItem, inputMap);
        assertThat(value, is(true));
    }
}