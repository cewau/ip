package zucc.ui;

import java.util.Scanner;

/**
 * Handles terminal input and output for the Zucc command-line interface.
 */
public final class Ui implements AutoCloseable {
    /** A visual separator used to frame the chatbot's messages. */
    private static final String SEPARATOR =
            "____________________________________________________________";

    /** The number of spaces before each separator. */
    private static final int SEPARATOR_INDENT = 4;

    /** Messages are indented one space farther than their separators. */
    private static final int MESSAGE_INDENT = SEPARATOR_INDENT + 1;

    /** Logo and welcome message shown when the application starts. */
    private static final String GREETING = " ______                \n"
            + "|___  /                \n"
            + "   / / _   _  ___ ___  \n"
            + "  / / | | | |/ __/ __| \n"
            + " / /__| |_| | (_| (__  \n"
            + "/_____|\\__,_|\\___\\___|\n"
            + "Hello! I'm Zucc.\n"
            + "What can I do for you?";

    /** Source from which user commands are read. */
    private final Scanner scanner;

    /**
     * Creates a terminal UI connected to standard input.
     */
    public Ui() {
        scanner = new Scanner(System.in);
    }

    /**
     * Reports whether another command is available from the user.
     *
     * @return {@code true} if another input line can be read
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads the next command line entered by the user.
     *
     * @return raw command text
     */
    public String readCommand() {
        return scanner.nextLine();
    }

    /**
     * Shows the application logo and welcome message.
     */
    public void showGreeting() {
        showMessage(GREETING);
    }

    /**
     * Prints a response between separators using the required indentation.
     * Multiline responses are indented line by line.
     *
     * @param message response to print
     */
    public void showMessage(String message) {
        System.out.print(SEPARATOR.indent(SEPARATOR_INDENT));
        System.out.print(message.indent(MESSAGE_INDENT));
        System.out.print(SEPARATOR.indent(SEPARATOR_INDENT));
    }

    /**
     * Releases the input scanner when the application finishes.
     */
    @Override
    public void close() {
        scanner.close();
    }
}
