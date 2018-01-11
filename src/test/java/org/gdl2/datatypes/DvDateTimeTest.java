package org.gdl2.datatypes;

import org.testng.annotations.Test;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DvDateTimeTest {

    @Test
    public void can_parse_iso_datetime_value_without_timezone() {
        LocalDateTime dateTime = DvDateTime.valueOf("2012-01-10T05:07:15").getDateTime();
        assertThat(dateTime.getYear(), is(2012));
        assertThat(dateTime.getMonthValue(), is(1));
        assertThat(dateTime.getDayOfMonth(), is(10));
        assertThat(dateTime.getHour(), is(5));
        assertThat(dateTime.getMinute(), is(7));
        assertThat(dateTime.getSecond(), is(15));
    }

    @Test
    public void can_parse_iso_datetime_value_timezone() {
        DvDateTime.valueOf("2012-01-10T05:07:15+01:00");
    }

    @Test
    public void can_get_date() {
        DvDateTime dateTime = DvDateTime.valueOf("2012-01-10T05:07:15");
        DvDate dvDate = dateTime.date();
        assertThat(dvDate.toString(), is("2012-01-10"));
    }

    @Test
    public void can_print_iso_formatted_string() {
        DvDateTime dateTime = DvDateTime.valueOf("2012-01-10T05:07:15");
        assertThat(dateTime.toString(), is("2012-01-10T05:07:15"));
    }

    @Test
    public void can_print_iso_formatted_string_with_zero_seconds() {
        DvDateTime dateTime = DvDateTime.valueOf("2012-01-10T05:07:00");
        assertThat(dateTime.toString(), is("2012-01-10T05:07:00"));
    }
}
