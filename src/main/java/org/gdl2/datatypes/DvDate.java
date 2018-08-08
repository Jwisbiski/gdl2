package org.gdl2.datatypes;

import lombok.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Deprecated
@Value
public final class DvDate {
    private LocalDate date;

    DvDate(LocalDate date) {
        this.date = date;
    }

    public DvDate() {
        this(LocalDate.now());
    }

    public static DvDate valueOf(String value) {
        LocalDate date = LocalDate.parse(value);
        return new DvDate(date);
    }

    @Override
    public String toString() {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
    }
}