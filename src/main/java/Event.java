/**
 * Represents a task that takes place between a start and an end date or time.
 * Both values are stored as text so that users may choose a convenient format.
 */
public class Event extends Task {
    /** Error used when an event is missing any of its required values. */
    private static final String INVALID_EVENT_ERROR =
            "Zucc needs more data: add an event description "
                    + "followed by /from and /to times.";

    /** The user-provided date or time at which the event starts. */
    private final String from;

    /** The user-provided date or time at which the event ends. */
    private final String to;

    /**
     * Creates an incomplete event with the given description and time range.
     *
     * @param description description of the event
     * @param from user-provided start date or time
     * @param to user-provided end date or time
     * @throws ZuccException if the description, start, or end is blank
     */
    public Event(String description, String from, String to) throws ZuccException {
        super(requireNonBlank(description, INVALID_EVENT_ERROR));
        this.from = requireNonBlank(from, INVALID_EVENT_ERROR);
        this.to = requireNonBlank(to, INVALID_EVENT_ERROR);
    }

    /**
     * Formats this task with its type marker and time range.
     *
     * @return the task in {@code [E][status] description (from: start to: end)} format
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
