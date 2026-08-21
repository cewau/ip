import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Parses and holds a command type and the values extracted from one input line.
 * This class does not know which options belong to each command; callers declare
 * the allowed options when handling the command.
 */
public final class ParsedCommand {
    /** Message used when the first word does not identify a supported command. */
    private static final String UNKNOWN_COMMAND_ERROR =
            "Zucc's algorithm doesn't recognize that command. "
                    + "Try todo, deadline, event, list, mark, unmark, delete, or bye.";

    /** The command word represented as a known enum value. */
    private final CommandType type;

    /** Free-form text between the command word and the first named option. */
    private final String argument;

    /** Parsed option values indexed by separators such as {@code /by}. */
    private final Map<String, String> options;

    /**
     * Creates a parsed command from a command word, its main argument, and any
     * named options.
     * Every space-delimited token beginning with a slash starts an option, whose
     * value continues until the next option separator. Empty tokens preserve
     * repeated spaces inside arguments and option values.
     *
     * @param input complete line entered by the user
     * @throws ZuccException if the command is unknown or an option is duplicated
     */
    public ParsedCommand(String input) throws ZuccException {
        String normalizedInput = input.strip();
        Iterator<String> words = Arrays.asList(normalizedInput.split(" ", -1)).iterator();
        String keyword = words.next();
        CommandType type = null;
        for (CommandType candidate : CommandType.values()) {
            if (candidate.getKeyword().equals(keyword)) {
                type = candidate;
                break;
            }
        }
        if (type == null) {
            throw new ZuccException(UNKNOWN_COMMAND_ERROR);
        }

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

        this.type = type;
        this.argument = mainArgument;
        this.options = Collections.unmodifiableMap(new LinkedHashMap<>(options));
    }

    /**
     * Returns the recognized command type.
     *
     * @return command type
     */
    public CommandType getType() {
        return type;
    }

    /**
     * Returns the free-form main argument.
     *
     * @return text preceding the first named option
     */
    public String getArgument() {
        return argument;
    }

    /**
     * Returns the value of an option required by the current command.
     *
     * @param optionName option separator, including its leading slash
     * @return parsed option value
     * @throws ZuccException if the option was not supplied
     */
    public String getRequiredOption(String optionName) throws ZuccException {
        String value = options.get(optionName);
        if (value == null) {
            throw new ZuccException("Zucc needs more data: the "
                    + type.getKeyword() + " command requires " + optionName + ".");
        }
        return value;
    }

    /**
     * Ensures that no main argument or named option followed the command word.
     *
     * @throws ZuccException if the user supplied any additional input
     */
    public void requireNoArguments() throws ZuccException {
        rejectUnexpectedOptions();
        if (!argument.isEmpty()) {
            throw new ZuccException("Zucc doesn't expect additional data for the "
                    + type.getKeyword() + " command.");
        }
    }

    /**
     * Rejects options that are not supported by the current command.
     * Calling this method without arguments rejects every supplied option.
     *
     * @param allowedOptions option separators accepted by the command
     * @throws ZuccException if the user supplied an option not in the allowed set
     */
    public void rejectUnexpectedOptions(String... allowedOptions) throws ZuccException {
        Set<String> allowed = Set.of(allowedOptions);
        for (String suppliedOption : options.keySet()) {
            if (!allowed.contains(suppliedOption)) {
                throw new ZuccException("Zucc doesn't recognize option "
                        + suppliedOption + " for the " + type.getKeyword() + " command.");
            }
        }
    }
}
