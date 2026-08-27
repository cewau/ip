import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Defines how task dates and times are read, displayed, and stored.
 * Keeping this policy in one place gives deadlines and events consistent behavior.
 */
final class TaskDateTimeFormat {
    /** Format accepted in commands and used in storage. */
    private static final DateTimeFormatter INPUT_FORMAT =
            DateTimeFormatter.ofPattern("d/M/uuuu HHmm", Locale.ENGLISH)
                    .withResolverStyle(ResolverStyle.STRICT);

    /** User-friendly format used in chatbot responses. */
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd uuuu, h:mma", Locale.ENGLISH);

    /** Prevents instantiation of this formatting utility. */
    private TaskDateTimeFormat() {
    }

    /**
     * Parses a task date and time strictly, including calendar validation.
     *
     * @param value date and time in {@code d/M/yyyy HHmm} format
     * @return parsed date and time
     * @throws ZuccException if the value is blank, malformed, or impossible
     */
    static LocalDateTime parse(String value) throws ZuccException {
        try {
            return LocalDateTime.parse(value, INPUT_FORMAT);
        } catch (DateTimeParseException exception) {
            throw new ZuccException(
                    "Zucc can't understand that date and time. "
                            + "Use d/M/yyyy HHmm, for example 2/12/2019 1800.",
                    exception);
        }
    }

    /**
     * Formats a date and time for display to the user.
     *
     * @param value date and time to format
     * @return value in a user-friendly format
     */
    static String formatForDisplay(LocalDateTime value) {
        return value.format(DISPLAY_FORMAT);
    }

    /**
     * Formats a date and time in the same stable form accepted by the parser.
     *
     * @param value date and time to store
     * @return value in persistent form
     */
    static String formatForStorage(LocalDateTime value) {
        return value.format(INPUT_FORMAT);
    }
}
