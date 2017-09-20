package org.gdl2.datatypes;

import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertNotNull;

public class DvDateTest {
    private DvDate dvDate;

    @Test
    public void can_parse_iso_datetime_value_without_timezone() {
        LocalDate date = DvDate.valueOf("2012-01-10").getDate();
        assertThat(date.getYear(), is(2012));
        assertThat(date.getMonthValue(), is(1));
        assertThat(date.getDayOfMonth(), is(10));
    }

    @Test(expectedExceptions = DateTimeParseException.class)
    public void can_parse_iso_datetime_value_timezone_expect_parsing_failure() {
        DvDate.valueOf("2012-01-10+01:00");
    }

    @Test
    public void can_print_iso_formatted_string() {
        dvDate = DvDate.valueOf("2012-01-10");
        assertThat(dvDate.toString(), is("2012-01-10"));
    }

    @Test
    public void can_print_iso_formatted_string_with_zero_seconds() {
        dvDate = DvDate.valueOf("2012-01-10");
        assertThat(dvDate.toString(), is("2012-01-10"));
    }

    @Test
    public void can_create_new_date() {
        dvDate = new DvDate();
        assertNotNull(dvDate);
    }
}
