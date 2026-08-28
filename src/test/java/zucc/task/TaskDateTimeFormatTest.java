package zucc.task;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import zucc.ZuccException;

/**
 * Tests strict parsing and stable formatting of task dates and times.
 */
public class TaskDateTimeFormatTest {
    @Test
    public void parse_validSingleDigitDateTime_parsedValueReturned() throws ZuccException {
        assertEquals(
                LocalDateTime.of(2026, 9, 2, 8, 5),
                TaskDateTimeFormat.parse("2/9/2026 0805"));
    }

    @Test
    public void parse_validLeapDay_parsedValueReturned() throws ZuccException {
        assertEquals(
                LocalDateTime.of(2028, 2, 29, 23, 59),
                TaskDateTimeFormat.parse("29/2/2028 2359"));
    }

    @Test
    public void parse_malformedImpossibleOrInvalidTime_exceptionThrown() {
        assertAll(
                () -> assertThrows(ZuccException.class,
                        () -> TaskDateTimeFormat.parse("2026-09-02 0805")),
                () -> assertThrows(ZuccException.class,
                        () -> TaskDateTimeFormat.parse("29/2/2027 0805")),
                () -> assertThrows(ZuccException.class,
                        () -> TaskDateTimeFormat.parse("2/9/2026 2400")),
                () -> assertThrows(ZuccException.class,
                        () -> TaskDateTimeFormat.parse("   ")));
    }

    @Test
    public void parseDate_validDate_parsedValueReturned() throws ZuccException {
        assertEquals(LocalDate.of(2026, 9, 2), TaskDateTimeFormat.parseDate("2/9/2026"));
    }

    @Test
    public void parseDate_malformedOrImpossibleDate_exceptionThrown() {
        assertAll(
                () -> assertThrows(ZuccException.class,
                        () -> TaskDateTimeFormat.parseDate("2026-09-02")),
                () -> assertThrows(ZuccException.class,
                        () -> TaskDateTimeFormat.parseDate("31/4/2026")),
                () -> assertThrows(ZuccException.class,
                        () -> TaskDateTimeFormat.parseDate("")));
    }

    @Test
    public void formatMethods_dateTime_returnExpectedDisplayAndStorageForms() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 9, 2, 8, 5);

        assertAll(
                () -> assertEquals(
                        "Sep 02 2026, 8:05AM",
                        TaskDateTimeFormat.formatForDisplay(dateTime)),
                () -> assertEquals(
                        "2/9/2026 0805",
                        TaskDateTimeFormat.formatForStorage(dateTime)),
                () -> assertEquals(
                        "Sep 02 2026",
                        TaskDateTimeFormat.formatDateForDisplay(dateTime.toLocalDate())));
    }
}
