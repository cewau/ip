/**
 * Identifies the commands understood by Zucc after their text has been parsed.
 * Using an enum ensures that the rest of the application handles only known
 * command types rather than repeatedly comparing raw strings.
 */
public enum CommandType {
    TODO("todo"),
    DEADLINE("deadline"),
    EVENT("event"),
    LIST("list"),
    MARK("mark"),
    UNMARK("unmark"),
    DELETE("delete"),
    BYE("bye");

    /** The word users enter to invoke this command. */
    private final String keyword;

    /**
     * Creates a command type associated with its user-facing keyword.
     *
     * @param keyword word that identifies the command in user input
     */
    CommandType(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the user-facing word for this command.
     *
     * @return command keyword
     */
    public String getKeyword() {
        return keyword;
    }
}
