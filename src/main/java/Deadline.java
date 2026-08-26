/**
 * Represents a task that must be completed by a given date or time.
 * The deadline is stored as text so that users may enter any useful description.
 */
public class Deadline extends Task {
    /** Short identifier used when displaying and storing deadlines. */
    static final String TYPE_CODE = "D";

    /** Error used when a deadline is missing any of its required values. */
    private static final String INVALID_DEADLINE_ERROR =
            "Zucc needs more data: add a deadline description "
                    + "followed by /by and a due date.";

    /** The user-provided date or time by which the task should be completed. */
    private final String by;

    /**
     * Creates an incomplete deadline with the given description and due date or time.
     *
     * @param description description of the task
     * @param by user-provided deadline text
     * @throws ZuccException if the description or deadline is blank
     */
    public Deadline(String description, String by) throws ZuccException {
        super(requireNonBlank(description, INVALID_DEADLINE_ERROR));
        this.by = requireNonBlank(by, INVALID_DEADLINE_ERROR);
    }

    /**
     * Reconstructs a deadline from decoded storage fields.
     * The field-count check runs before the superclass constructor so indexing is safe.
     *
     * @param fields decoded type, status, description, and deadline
     * @throws ZuccException if the fields do not describe a valid deadline
     */
    Deadline(String[] fields) throws ZuccException {
        if (fields == null || fields.length != 4) {
            throw new ZuccException("Invalid stored deadline.");
        }
        super(fields[2], fields[1]);
        this.by = requireNonBlank(fields[3], INVALID_DEADLINE_ERROR);
    }

    /**
     * Supplies the plain subtype fields needed to store this deadline.
     *
     * @return type code followed by the deadline
     */
    @Override
    protected String[] getStorageFields() {
        return new String[]{TYPE_CODE, by};
    }

    /**
     * Formats this task with its type marker and deadline.
     *
     * @return the task in {@code [D][status] description (by: deadline)} format
     */
    @Override
    public String toString() {
        return "[" + TYPE_CODE + "]" + super.toString() + " (by: " + by + ")";
    }
}
