/**
 * Represents a task that has no date or time attached to it.
 */
public class Todo extends Task {
    /** Error used when a to-do is missing its required description. */
    private static final String INVALID_TODO_ERROR =
            "Zucc needs more data: give that todo a description.";

    /**
     * Creates an incomplete to-do with the given description.
     *
     * @param description description of the task
     * @throws ZuccException if the description is blank
     */
    public Todo(String description) throws ZuccException {
        super(requireNonBlank(description, INVALID_TODO_ERROR));
    }

    /**
     * Formats this task with the type marker used for to-dos.
     *
     * @return the task in {@code [T][status] description} format
     */
    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
