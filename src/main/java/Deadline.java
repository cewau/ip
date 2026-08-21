/**
 * Represents a task that must be completed by a given date or time.
 * The deadline is stored as text so that users may enter any useful description.
 */
public class Deadline extends Task {
    /** The user-provided date or time by which the task should be completed. */
    private final String by;

    /**
     * Creates an incomplete deadline with the given description and due date or time.
     *
     * @param description description of the task
     * @param by user-provided deadline text
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * Formats this task with its type marker and deadline.
     *
     * @return the task in {@code [D][status] description (by: deadline)} format
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
