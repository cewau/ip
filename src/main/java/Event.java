import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a task that takes place between a start and an end date or time.
 */
public class Event extends Task {
    /** Short identifier used when displaying and storing events. */
    static final String TYPE_CODE = "E";

    /** Error used when an event is missing any of its required values. */
    private static final String INVALID_EVENT_ERROR =
            "Zucc needs more data: add an event description "
                    + "followed by /from and /to times.";

    /** Error used when an event ends before it starts. */
    private static final String INVALID_EVENT_RANGE_ERROR =
            "Zucc can't schedule an event that ends before it starts.";

    /** Date and time at which the event starts. */
    private final LocalDateTime from;

    /** Date and time at which the event ends. */
    private final LocalDateTime to;

    /**
     * Creates an incomplete event with the given description and time range.
     *
     * @param description description of the event
     * @param from start date and time in {@code d/M/yyyy HHmm} format
     * @param to end date and time in {@code d/M/yyyy HHmm} format
     * @throws ZuccException if a required value or date range is invalid
     */
    public Event(String description, String from, String to) throws ZuccException {
        super(requireNonBlank(description, INVALID_EVENT_ERROR));
        this.from = TaskDateTimeFormat.parse(
                requireNonBlank(from, INVALID_EVENT_ERROR));
        this.to = TaskDateTimeFormat.parse(
                requireNonBlank(to, INVALID_EVENT_ERROR));
        requireValidRange();
    }

    /**
     * Reconstructs an event from decoded storage fields.
     * The field-count check runs before the superclass constructor so indexing is safe.
     *
     * @param fields decoded type, status, description, start, and end
     * @throws ZuccException if the fields do not describe a valid event
     */
    Event(String[] fields) throws ZuccException {
        if (fields == null || fields.length != 5) {
            throw new ZuccException("Invalid stored event.");
        }
        super(fields[2], fields[1]);
        this.from = TaskDateTimeFormat.parse(
                requireNonBlank(fields[3], INVALID_EVENT_ERROR));
        this.to = TaskDateTimeFormat.parse(
                requireNonBlank(fields[4], INVALID_EVENT_ERROR));
        requireValidRange();
    }

    /**
     * Ensures that this event's end does not precede its start.
     *
     * @throws ZuccException if the event has a negative duration
     */
    private void requireValidRange() throws ZuccException {
        if (to.isBefore(from)) {
            throw new ZuccException(INVALID_EVENT_RANGE_ERROR);
        }
    }

    /**
     * Reports whether any part of this event occurs on a given date.
     * Both the start and end dates are included.
     *
     * @param date date to check
     * @return {@code true} if the event occurs on the date
     */
    @Override
    public boolean occursOn(LocalDate date) {
        return !date.isBefore(from.toLocalDate())
                && !date.isAfter(to.toLocalDate());
    }

    /**
     * Supplies the plain subtype fields needed to store this event.
     *
     * @return type code followed by the start and end times
     */
    @Override
    protected String[] getStorageFields() {
        return new String[]{
            TYPE_CODE,
            TaskDateTimeFormat.formatForStorage(from),
            TaskDateTimeFormat.formatForStorage(to)
        };
    }

    /**
     * Formats this task with its type marker and time range.
     *
     * @return the task in {@code [E][status] description (from: start to: end)} format
     */
    @Override
    public String toString() {
        return "[" + TYPE_CODE + "]" + super.toString()
                + " (from: " + TaskDateTimeFormat.formatForDisplay(from)
                + " to: " + TaskDateTimeFormat.formatForDisplay(to) + ")";
    }
}
