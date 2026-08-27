import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Holds a command type and the values that {@link Parser} extracted from one input line.
 * This class does not know which options belong to each command; callers declare
 * the allowed options when handling the command.
 */
public final class ParsedCommand {
    /** The command word represented as a known enum value. */
    private final CommandType type;

    /** Free-form text between the command word and the first named option. */
    private final String argument;

    /** Parsed option values indexed by separators such as {@code /by}. */
    private final Map<String, String> options;

    /**
     * Creates a parsed command from values produced by {@link Parser}.
     *
     * @param type recognized command type
     * @param argument free-form main argument
     * @param options named option values indexed by their separators
     */
    ParsedCommand(CommandType type, String argument, Map<String, String> options) {
        this.type = type;
        this.argument = argument;
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
