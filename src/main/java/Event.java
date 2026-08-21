/**
 * Represents a task that takes place between a start and an end date or time.
 * Both values are stored as text so that users may choose a convenient format.
 */
public class Event extends Task {
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
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
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
