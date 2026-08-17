import java.util.Scanner;

/**
 * Starts the Zucc chatbot application.
 */
public class Zucc {
    /** A visual separator used to frame the chatbot's messages. */
    private static final String SEPARATOR = "____________________________________________________________";

    /**
     * Greets the user, echoes commands, and exits when the user enters {@code bye}.
     *
     * @param args command-line arguments; not used by this application
     */
    public static void main(String[] args) {
        String banner = " ______                \n"
                + "|___  /                \n"
                + "   / / _   _  ___ ___  \n"
                + "  / / | | | |/ __/ __| \n"
                + " / /__| |_| | (_| (__  \n"
                + "/_____|\\__,_|\\___\\___|\n";

        System.out.println(SEPARATOR);
        System.out.print(banner);
        System.out.println("Hello! I'm Zucc.");
        System.out.println("What can I do for you?");
        System.out.println(SEPARATOR);

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();

                if (command.equals("bye")) {
                    System.out.println(SEPARATOR);
                    System.out.println("Bye. Hope to see you again soon!");
                    System.out.println(SEPARATOR);
                    break;
                }

                System.out.println(SEPARATOR);
                System.out.println(command);
                System.out.println(SEPARATOR);
            }
        }
    }
}
