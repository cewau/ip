package zucc.task;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import zucc.ZuccException;

/**
 * Tests event validation, date matching, persistence fields, and display formatting.
 */
public class EventTest {
    private static final String DESCRIPTION = "Attend software engineering workshop";
    private static final String START = "10/6/2026 2300";
    private static final String END = "12/6/2026 0100";

    private Event multiDayEvent;

    @BeforeEach
    public void setUp() throws ZuccException {
        multiDayEvent = new Event(DESCRIPTION, START, END);
    }

    @Test
    public void constructor_nullOrBlankDescription_exceptionThrown() {
        assertAll(
                () -> assertThrows(ZuccException.class,
                        () -> new Event(null, START, END)),
                () -> assertThrows(ZuccException.class,
                        () -> new Event("", START, END)),
                () -> assertThrows(ZuccException.class,
                        () -> new Event("   ", START, END)));
    }

    @Test
    public void constructor_nullOrBlankStart_exceptionThrown() {
        assertAll(
                () -> assertThrows(ZuccException.class,
                        () -> new Event(DESCRIPTION, null, END)),
                () -> assertThrows(ZuccException.class,
                        () -> new Event(DESCRIPTION, "", END)),
                () -> assertThrows(ZuccException.class,
                        () -> new Event(DESCRIPTION, "   ", END)));
    }

    @Test
    public void constructor_nullOrBlankEnd_exceptionThrown() {
        assertAll(
                () -> assertThrows(ZuccException.class,
                        () -> new Event(DESCRIPTION, START, null)),
                () -> assertThrows(ZuccException.class,
                        () -> new Event(DESCRIPTION, START, "")),
                () -> assertThrows(ZuccException.class,
                        () -> new Event(DESCRIPTION, START, "   ")));
    }

    @Test
    public void constructor_malformedStart_exceptionThrown() {
        assertThrows(ZuccException.class,
                () -> new Event(DESCRIPTION, "not a date", END));
    }

    @Test
    public void constructor_impossibleEndDate_exceptionThrown() {
        assertThrows(ZuccException.class,
                () -> new Event(DESCRIPTION, START, "31/2/2027 1200"));
    }

    @Test
    public void constructor_endBeforeStart_exceptionThrown() {
        assertThrows(ZuccException.class,
                () -> new Event(DESCRIPTION, "12/6/2026 0100", "10/6/2026 2300"));
    }

    @Test
    public void constructor_sameStartAndEnd_eventCreated() throws ZuccException {
        Event zeroDurationEvent = new Event(
                "Submit project", "15/6/2026 1200", "15/6/2026 1200");

        assertTrue(zeroDurationEvent.occursOn(LocalDate.of(2026, 6, 15)));
    }

    @Test
    public void storageConstructor_validFields_eventRestored() throws ZuccException {
        Event restoredEvent = new Event(new String[]{
            "E", "1", DESCRIPTION, START, END
        });

        assertEquals(
                "[E][X] Attend software engineering workshop "
                        + "(from: Jun 10 2026, 11:00PM to: Jun 12 2026, 1:00AM)",
                restoredEvent.toString());
    }

    @Test
    public void storageConstructor_nullOrIncorrectFieldCount_exceptionThrown() {
        assertAll(
                () -> assertThrows(ZuccException.class,
                        () -> new Event((String[]) null)),
                () -> assertThrows(ZuccException.class,
                        () -> new Event(new String[]{"E", "0", DESCRIPTION, START})),
                () -> assertThrows(ZuccException.class,
                        () -> new Event(new String[]{
                            "E", "0", DESCRIPTION, START, END, "extra"
                        })));
    }

    @Test
    public void storageConstructor_invalidCompletionStatus_exceptionThrown() {
        assertThrows(ZuccException.class,
                () -> new Event(new String[]{"E", "done", DESCRIPTION, START, END}));
    }

    @Test
    public void storageConstructor_endBeforeStart_exceptionThrown() {
        assertThrows(ZuccException.class,
                () -> new Event(new String[]{
                    "E", "0", DESCRIPTION, "12/6/2026 0100", "10/6/2026 2300"
                }));
    }

    @Test
    public void occursOn_dateBeforeEvent_returnsFalse() {
        assertFalse(multiDayEvent.occursOn(LocalDate.of(2026, 6, 9)));
    }

    @Test
    public void occursOn_eventStartDate_returnsTrue() {
        assertTrue(multiDayEvent.occursOn(LocalDate.of(2026, 6, 10)));
    }

    @Test
    public void occursOn_dateBetweenStartAndEnd_returnsTrue() {
        assertTrue(multiDayEvent.occursOn(LocalDate.of(2026, 6, 11)));
    }

    @Test
    public void occursOn_eventEndDate_returnsTrue() {
        assertTrue(multiDayEvent.occursOn(LocalDate.of(2026, 6, 12)));
    }

    @Test
    public void occursOn_dateAfterEvent_returnsFalse() {
        assertFalse(multiDayEvent.occursOn(LocalDate.of(2026, 6, 13)));
    }

    @Test
    public void occursOn_sameDayEventDate_returnsTrue() throws ZuccException {
        Event sameDayEvent = new Event(
                "Attend tutorial",
                "15/6/2026 1000",
                "15/6/2026 1200");

        assertTrue(sameDayEvent.occursOn(LocalDate.of(2026, 6, 15)));
    }

    @Test
    public void occursOn_eventSpanningYearBoundary_bothDatesReturnTrue()
            throws ZuccException {
        Event newYearEvent = new Event(
                "Attend countdown",
                "31/12/2026 2300",
                "1/1/2027 0100");

        assertAll(
                () -> assertTrue(newYearEvent.occursOn(LocalDate.of(2026, 12, 31))),
                () -> assertTrue(newYearEvent.occursOn(LocalDate.of(2027, 1, 1))));
    }

    @Test
    public void getStorageFields_event_returnsTypeAndTimeRange() {
        assertArrayEquals(
                new String[]{"E", "10/6/2026 2300", "12/6/2026 0100"},
                multiDayEvent.getStorageFields());
    }

    @Test
    public void toString_incompleteEvent_returnsFormattedEvent() {
        assertEquals(
                "[E][ ] Attend software engineering workshop "
                        + "(from: Jun 10 2026, 11:00PM to: Jun 12 2026, 1:00AM)",
                multiDayEvent.toString());
    }
}
