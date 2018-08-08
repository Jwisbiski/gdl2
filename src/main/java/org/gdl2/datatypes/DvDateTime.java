package org.gdl2.datatypes;

import lombok.Value;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Deprecated
@Value
public final class DvDateTime {
    private LocalDateTime dateTime;

    private DvDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public DvDateTime() {
        this(LocalDateTime.now());
    }

    public static DvDateTime valueOf(String value) {
        LocalDateTime dateTime = LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
        return new DvDateTime(dateTime);
    }

    public DvDate date() {
        return new DvDate(this.dateTime.toLocalDate());
    }

    @Override
    public String toString() {
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime);
    }
}