package zucc.task;

import java.time.LocalDate;
import java.util.regex.Pattern;

import zucc.ZuccException;

/**
 * Represents a task and whether it has been completed.
 */
public abstract class Task {
    /** Separator used between fields in one persistent task record. */
    static final String STORAGE_FIELD_SEPARATOR = " | ";

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
     * @param description description of the task.
     * @throws ZuccException if the description is blank
     */
    public Task(String description) throws ZuccException {
        this.description = requireNonBlank(description, MISSING_DESCRIPTION_ERROR);
        this.isDone = false;
    }

    /**
     * Reconstructs the common fields of a task from decoded storage values.
     *
     * @param description decoded task description.
     * @param status {@code 1} for done or {@code 0} for not done.
     * @throws ZuccException if the description or completion status is invalid
     */
    protected Task(String description, String status) throws ZuccException {
        this(description);
        if ("1".equals(status)) {
            isDone = true;
        } else if (!"0".equals(status)) {
            throw new ZuccException("Invalid stored completion status.");
        }
    }

    /**
     * Returns a required value after ensuring it contains meaningful text.
     * Subclasses use this helper to enforce their own constructor invariants
     * while retaining command-specific error messages.
     *
     * @param value required value.
     * @param errorMessage message to use if the value is blank.
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
     * Decodes a stored record and routes its plain fields to the identified task subtype.
     * Concrete task constructors validate the number and meaning of their own fields.
     *
     * @param line complete stored task record.
     * @return task reconstructed from the record
     * @throws ZuccException if the record has an unknown type or invalid fields
     */
    public static Task fromStorageString(String line) throws ZuccException {
        String[] fields = line.split(Pattern.quote(STORAGE_FIELD_SEPARATOR), -1);
        for (int i = 0; i < fields.length; i++) {
            fields[i] = decodeStorageField(fields[i]);
        }

        return switch (fields[0]) {
            case Todo.TYPE_CODE -> new Todo(fields);
            case Deadline.TYPE_CODE -> new Deadline(fields);
            case Event.TYPE_CODE -> new Event(fields);
            default -> throw new ZuccException("Unknown stored task type.");
        };
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
     * Reports whether this task occurs on a given date.
     *
     * @param date date to check.
     * @return {@code true} if this task occurs on the date
     */
    public abstract boolean occursOn(LocalDate date);

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
     * Supplies the plain subtype-specific fields needed to save this task.
     * The first element is the type code; remaining elements belong to the subtype.
     *
     * @return unencoded type and subtype-specific fields
     */
    protected abstract String[] getStorageFields();

    /**
     * Formats this task as one line containing all data needed to reconstruct it.
     * Separators in plain task fields are escaped to keep the record unambiguous.
     *
     * @return persistent representation of this task
     */
    public final String toStorageString() {
        String[] storageFields = getStorageFields();
        StringBuilder line = new StringBuilder(storageFields[0])
                .append(STORAGE_FIELD_SEPARATOR)
                .append(isDone ? "1" : "0")
                .append(STORAGE_FIELD_SEPARATOR)
                .append(encodeStorageField(description));

        for (int i = 1; i < storageFields.length; i++) {
            line.append(STORAGE_FIELD_SEPARATOR)
                    .append(encodeStorageField(storageFields[i]));
        }
        return line.toString();
    }

    /**
     * Escapes characters that have structural meaning in the storage format.
     * The percent sign is escaped first so loading can safely reverse the operations.
     *
     * @param field user-provided field value.
     * @return escaped field value
     */
    private static String encodeStorageField(String field) {
        return field.replace("%", "%25").replace("|", "%7C");
    }

    /**
     * Restores a field escaped while saving a task.
     *
     * @param field escaped field value.
     * @return original user-provided value
     */
    private static String decodeStorageField(String field) {
        return field.replace("%7C", "|").replace("%25", "%");
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
