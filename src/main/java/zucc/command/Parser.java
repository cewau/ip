package zucc.command;

import java.util.Arrays;
import java.util.Iterator;

import zucc.ZuccException;

/**
 * Recognizes raw user input and populates the corresponding concrete command.
 * TODO: Deliberate whether parsing merits a separate class or should be nested
 * inside Command to keep command creation and parsing in one place.
 */
final class Parser {
    /** Message used when the first word does not identify a supported command. */
    private static final String UNKNOWN_COMMAND_ERROR =
            "Zucc's algorithm doesn't recognize that command. "
                    + "Try todo, deadline, event, list, on, mark, unmark, delete, or bye.";

    /** Prevents creation of a utility class that contains only parsing operations. */
    private Parser() {
    }

    /**
     * Parses a complete input line directly into a concrete command.
     * Every space-delimited token beginning with a slash starts an option, whose
     * value continues until the next option separator. Empty tokens preserve
     * repeated spaces inside arguments and option values.
     *
     * @param input complete line entered by the user.
     * @return parsed command.
     * @throws ZuccException if the command is unknown or an option is unsupported
     *         or duplicated.
     */
    static Command parse(String input) throws ZuccException {
        String normalizedInput = input.strip();
        Iterator<String> words = Arrays.asList(normalizedInput.split(" ", -1)).iterator();
        Command command = createCommand(words.next());

        String currentOption = null;
        StringBuilder currentValue = new StringBuilder();
        boolean hasValueTokens = false;

        while (true) {
            boolean isInputFinished = !words.hasNext();
            String word = isInputFinished ? "" : words.next();
            boolean isOption = !isInputFinished
                    && word.startsWith("/")
                    && word.length() > 1;

            if (isInputFinished || isOption) {
                String completedValue = currentValue.toString().strip();
                if (currentOption == null) {
                    command.setArgument(completedValue);
                } else {
                    command.addOption(currentOption, completedValue);
                }

                if (isInputFinished) {
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

        return command;
    }

    /**
     * Creates the concrete command associated with a recognized keyword.
     *
     * @param keyword first word of the input line.
     * @return empty command ready to receive its raw argument and options.
     * @throws ZuccException if the keyword does not identify a supported command.
     */
    private static Command createCommand(String keyword) throws ZuccException {
        return switch (keyword) {
            case "todo" -> new TodoCommand();
            case "deadline" -> new DeadlineCommand();
            case "event" -> new EventCommand();
            case "list" -> new ListCommand();
            case "on" -> new OnCommand();
            case "mark" -> new MarkCommand();
            case "unmark" -> new UnmarkCommand();
            case "delete" -> new DeleteCommand();
            case "bye" -> new ExitCommand();
            default -> throw new ZuccException(UNKNOWN_COMMAND_ERROR);
        };
    }
}
