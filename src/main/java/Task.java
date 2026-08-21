/**
 * Represents a task and whether it has been completed.
 */
public class Task {
    /** Description of the work to be completed. */
    protected String description;

    /** Whether this task has been completed. */
    protected boolean isDone;

    /**
     * Creates an incomplete task with the given description.
     *
     * @param description description of the task
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
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
