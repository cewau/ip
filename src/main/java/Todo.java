/**
 * Represents a task that has no date or time attached to it.
 */
public class Todo extends Task {
    /**
     * Creates an incomplete to-do with the given description.
     *
     * @param description description of the task
     */
    public Todo(String description) {
        super(description);
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
