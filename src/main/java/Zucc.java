import java.util.Scanner;

/**
 * Starts the Zucc chatbot application.
 */
public class Zucc {
    /** Maximum number of tasks that can be stored during one run. */
    private static final int MAX_TASKS = 100;

    /** A visual separator used to frame the chatbot's messages. */
    private static final String SEPARATOR = "____________________________________________________________";

    /** The number of spaces before each separator. */
    private static final int SEPARATOR_INDENT = 4;

    /** Messages are indented one space farther than their separators. */
    private static final int MESSAGE_INDENT = SEPARATOR_INDENT + 1;

    /**
     * Prints a response between separators using the required indentation.
     * Multiline responses, such as the banner and greeting, are indented line by line.
     *
     * @param message response to print
     */
    private static void printResponse(String message) {
        System.out.print(SEPARATOR.indent(SEPARATOR_INDENT));
        System.out.print(message.indent(MESSAGE_INDENT));
        System.out.print(SEPARATOR.indent(SEPARATOR_INDENT));
    }

    /**
     * Formats the stored tasks as a one-based numbered list.
     *
     * @param tasks array containing the stored tasks
     * @param taskCount number of tasks currently stored
     * @return all stored tasks, one per line
     */
    private static String formatTasks(Task[] tasks, int taskCount) {
        StringBuilder taskList = new StringBuilder();

        for (int i = 0; i < taskCount; i++) {
            if (i > 0) {
                taskList.append('\n');
            }
            taskList.append(i + 1)
                    .append('.')
                    .append(tasks[i]);
        }

        return taskList.toString();
    }

    /**
     * Greets the user, stores tasks, lists or updates their completion status
     * on request, and exits when the user enters {@code bye}.
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

        String greeting = banner
                + "Hello! I'm Zucc.\n"
                + "What can I do for you?";

        printResponse(greeting);

        // TODO: Handle attempts to add more than MAX_TASKS tasks.
        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();

                if (command.equals("bye")) {
                    printResponse("Bye. Hope to see you again soon!");
                    break;
                }

                if (command.equals("list")) {
                    String taskList = formatTasks(tasks, taskCount);
                    printResponse("Here are the tasks in your list:\n" + taskList);
                    continue;
                }

                if (command.startsWith("mark ")) {
                    // TODO: Validate that the mark command contains a valid one-based task index.
                    int taskIndex = Integer.parseInt(command.substring("mark ".length())) - 1;
                    tasks[taskIndex].markAsDone();
                    printResponse("Nice! I've marked this task as done:\n  "
                            + tasks[taskIndex]);
                    continue;
                }

                if (command.startsWith("unmark ")) {
                    // TODO: Validate that the unmark command contains a valid one-based task index.
                    int taskIndex = Integer.parseInt(command.substring("unmark ".length())) - 1;
                    tasks[taskIndex].markAsNotDone();
                    printResponse("OK, I've marked this task as not done yet:\n  "
                            + tasks[taskIndex]);
                    continue;
                }

                tasks[taskCount] = new Task(command);
                taskCount++;
                printResponse("added: " + command);
            }
        }
    }
}
