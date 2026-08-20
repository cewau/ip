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
     * @param isTaskDone completion status corresponding to each stored task
     * @param taskCount number of tasks currently stored
     * @return all stored tasks, one per line
     */
    private static String formatTasks(String[] tasks, boolean[] isTaskDone, int taskCount) {
        StringBuilder taskList = new StringBuilder();

        for (int i = 0; i < taskCount; i++) {
            if (i > 0) {
                taskList.append('\n');
            }
            taskList.append(i + 1)
                    .append('.')
                    .append(formatTask(tasks[i], isTaskDone[i]));
        }

        return taskList.toString();
    }

    /**
     * Formats one task with a checkbox that shows whether it is done.
     *
     * @param task description of the task
     * @param isDone whether the task has been completed
     * @return the task prefixed by its completion status
     */
    private static String formatTask(String task, boolean isDone) {
        return (isDone ? "[X] " : "[ ] ") + task;
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
        String[] tasks = new String[MAX_TASKS];
        boolean[] isTaskDone = new boolean[MAX_TASKS];
        int taskCount = 0;

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();

                if (command.equals("bye")) {
                    printResponse("Bye. Hope to see you again soon!");
                    break;
                }

                if (command.equals("list")) {
                    String taskList = formatTasks(tasks, isTaskDone, taskCount);
                    printResponse("Here are the tasks in your list:\n" + taskList);
                    continue;
                }

                if (command.startsWith("mark ")) {
                    // TODO: Validate that the mark command contains a valid one-based task index.
                    int taskIndex = Integer.parseInt(command.substring("mark ".length())) - 1;
                    // TODO: Validate that the task is not already marked as done.
                    isTaskDone[taskIndex] = true;
                    printResponse("Nice! I've marked this task as done:\n  "
                            + formatTask(tasks[taskIndex], isTaskDone[taskIndex]));
                    continue;
                }

                if (command.startsWith("unmark ")) {
                    // TODO: Validate that the unmark command contains a valid one-based task index.
                    int taskIndex = Integer.parseInt(command.substring("unmark ".length())) - 1;
                    // TODO: Validate that the task is currently marked as done.
                    isTaskDone[taskIndex] = false;
                    printResponse("OK, I've marked this task as not done yet:\n  "
                            + formatTask(tasks[taskIndex], isTaskDone[taskIndex]));
                    continue;
                }

                tasks[taskCount] = command;
                taskCount++;
                printResponse("added: " + command);
            }
        }
    }
}
