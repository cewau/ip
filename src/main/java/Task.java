/**
 * Represents a task and whether it has been completed.
 */
public class Task {
    /** Description of the work to be completed. */
    protected String description;

    /** Whether this task has been completed. */
    protected boolean isDone;

    /** Error used when a base task is created without a description. */
    private static final String MISSING_DESCRIPTION_ERROR =
            "Zucc needs more data: give that task a description.";

    /**
     * Creates an incomplete task with the given description.
     *
     * @param description description of the task
     * @throws ZuccException if the description is blank
     */
    public Task(String description) throws ZuccException {
        this.description = requireNonBlank(description, MISSING_DESCRIPTION_ERROR);
        this.isDone = false;
    }

    /**
     * Returns a required value after ensuring it contains meaningful text.
     * Subclasses use this helper to enforce their own constructor invariants
     * while retaining command-specific error messages.
     *
     * @param value required value
     * @param errorMessage message to use if the value is blank
     * @return the validated value
     * @throws ZuccException if the value is {@code null}, empty, or whitespace-only
     */
    protected static String requireNonBlank(String value, String errorMessage)
            throws ZuccException {
        if (value == null || value.isBlank()) {
            throw new ZuccException(errorMessage);
        }
        return value;
    }

    /**
     * Returns the character used to display the task's completion status.
     *
     * @return {@code X} when done, or a space when not done
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /**
     * Marks this task as completed.
     *
     * @throws ZuccException if the task is already completed
     */
    public void markAsDone() throws ZuccException {
        if (isDone) {
            throw new ZuccException("Zucc's records already show that task as done.");
        }
        isDone = true;
    }

    /**
     * Marks this task as not completed.
     *
     * @throws ZuccException if the task is already incomplete
     */
    public void markAsNotDone() throws ZuccException {
        if (!isDone) {
            throw new ZuccException("Zucc's records already show that task as not done.");
        }
        isDone = false;
    }

    /**
     * Formats this task with its completion status.
     *
     * @return the task in {@code [status] description} format
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
