import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts raw user input into values that the command-handling layer can use.
 */
public final class Parser {
    /** Message used when the first word does not identify a supported command. */
    private static final String UNKNOWN_COMMAND_ERROR =
            "Zucc's algorithm doesn't recognize that command. "
                    + "Try todo, deadline, event, list, on, mark, unmark, delete, or bye.";

    /** Error shown when a task number is missing or malformed. */
    private static final String INVALID_TASK_NUMBER_ERROR =
            "Zucc couldn't find that task in the records. Use list to check its number.";

    /** Prevents creation of a utility class that contains only parsing operations. */
    private Parser() {
    }

    /**
     * Parses a complete input line into a recognized command and its supplied values.
     * Every space-delimited token beginning with a slash starts an option, whose
     * value continues until the next option separator. Empty tokens preserve
     * repeated spaces inside arguments and option values.
     *
     * @param input complete line entered by the user
     * @return parsed command
     * @throws ZuccException if the command is unknown or an option is duplicated
     */
    public static ParsedCommand parse(String input) throws ZuccException {
        String normalizedInput = input.strip();
        Iterator<String> words = Arrays.asList(normalizedInput.split(" ", -1)).iterator();
        CommandType type = parseCommandType(words.next());

        String mainArgument = "";
        Map<String, String> options = new LinkedHashMap<>();
        String currentOption = null;
        StringBuilder currentValue = new StringBuilder();
        boolean hasValueTokens = false;

        while (true) {
            boolean inputFinished = !words.hasNext();
            String word = inputFinished ? "" : words.next();
            boolean isOption = !inputFinished
                    && word.startsWith("/")
                    && word.length() > 1;

            if (inputFinished || isOption) {
                String completedValue = currentValue.toString().strip();
                if (currentOption == null) {
                    mainArgument = completedValue;
                } else if (options.putIfAbsent(currentOption, completedValue) != null) {
                    throw new ZuccException("Zucc can't use " + currentOption
                            + " more than once in one command.");
                }

                if (inputFinished) {
                    break;
                }
                currentOption = word;
                currentValue.setLength(0);
                hasValueTokens = false;
                continue;
            }

            if (hasValueTokens) {
                currentValue.append(' ');
            }
            currentValue.append(word);
            hasValueTokens = true;
        }

        return new ParsedCommand(type, mainArgument, options);
    }

    /**
     * Converts a user-provided one-based task number to a list index.
     * Bounds validation remains the responsibility of {@link TaskList}, which
     * knows the current number of tasks.
     *
     * @param taskNumberText user-provided task number
     * @return corresponding zero-based list index
     * @throws ZuccException if the number is missing or malformed
     */
    public static int parseTaskIndex(String taskNumberText) throws ZuccException {
        try {
            return Integer.parseInt(taskNumberText) - 1;
        } catch (NumberFormatException ignored) {
            // Malformed input and unavailable task numbers use the same response.
        }
        throw new ZuccException(INVALID_TASK_NUMBER_ERROR);
    }

    /**
     * Finds the command type represented by a user-entered keyword.
     *
     * @param keyword first word of the input line
     * @return recognized command type
     * @throws ZuccException if the keyword does not identify a supported command
     */
    private static CommandType parseCommandType(String keyword) throws ZuccException {
        for (CommandType candidate : CommandType.values()) {
            if (candidate.getKeyword().equals(keyword)) {
                return candidate;
            }
        }
        throw new ZuccException(UNKNOWN_COMMAND_ERROR);
    }
}
