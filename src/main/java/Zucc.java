import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Starts the Zucc chatbot application.
 */
public class Zucc {
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
     * @param tasks list containing the stored tasks
     * @return all stored tasks, one per line
     */
    private static String formatTasks(List<Task> tasks) {
        StringBuilder taskList = new StringBuilder();

        for (int i = 0; i < tasks.size(); i++) {
            if (i > 0) {
                taskList.append('\n');
            }
            taskList.append(i + 1)
                    .append('.')
                    .append(tasks.get(i));
        }

        return taskList.toString();
    }

    /**
     * Converts a user-provided one-based task number to a list index.
     *
     * @param taskNumberText user-provided task number
     * @param taskCount number of tasks currently stored
     * @return the corresponding zero-based array index
     * @throws ZuccException if the number is missing, malformed, or out of range
     */
    private static int parseTaskIndex(String taskNumberText, int taskCount)
            throws ZuccException {
        try {
            int taskIndex = Integer.parseInt(taskNumberText) - 1;
            if (taskIndex >= 0 && taskIndex < taskCount) {
                return taskIndex;
            }
        } catch (NumberFormatException ignored) {
            // Malformed and unavailable task numbers use the same helpful response.
        }
        throw new ZuccException("Zucc couldn't find that task in the records. "
                + "Use list to check its number.");
    }

    /**
     * Adds a task and confirms the updated task count.
     *
     * @param tasks list in which to store the task
     * @param newTask task to add
     */
    private static void addTask(List<Task> tasks, Task newTask) {
        tasks.add(newTask);
        printResponse("Got it. I've added this task:\n  "
                + newTask
                + "\nNow you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Greets the user, stores tasks in a collection, lists, deletes, or updates
     * their completion status on request, and exits when the user enters {@code bye}.
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

        List<Task> tasks = new ArrayList<>();

        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                try {
                    ParsedCommand command = new ParsedCommand(scanner.nextLine());

                    switch (command.getType()) {
                    case BYE -> {
                        command.requireNoArguments();
                        printResponse("Bye. Hope to see you again soon!");
                        return;
                    }
                    case LIST -> {
                        command.requireNoArguments();
                        printResponse("Here are the tasks in your list:\n" + formatTasks(tasks));
                    }
                    case DELETE -> {
                        command.rejectUnexpectedOptions();
                        int taskIndex = parseTaskIndex(command.getArgument(), tasks.size());
                        Task removedTask = tasks.remove(taskIndex);
                        printResponse("Noted. I've removed this task:\n  "
                                + removedTask
                                + "\nNow you have " + tasks.size() + " tasks in the list.");
                    }
                    case MARK -> {
                        command.rejectUnexpectedOptions();
                        int taskIndex = parseTaskIndex(command.getArgument(), tasks.size());
                        tasks.get(taskIndex).markAsDone();
                        printResponse("Nice! I've marked this task as done:\n  "
                                + tasks.get(taskIndex));
                    }
                    case UNMARK -> {
                        command.rejectUnexpectedOptions();
                        int taskIndex = parseTaskIndex(command.getArgument(), tasks.size());
                        tasks.get(taskIndex).markAsNotDone();
                        printResponse("OK, I've marked this task as not done yet:\n  "
                                + tasks.get(taskIndex));
                    }
                    case TODO -> {
                        command.rejectUnexpectedOptions();
                        addTask(tasks, new Todo(command.getArgument()));
                    }
                    case DEADLINE -> {
                        command.rejectUnexpectedOptions("/by");
                        String by = command.getRequiredOption("/by");
                        addTask(tasks, new Deadline(command.getArgument(), by));
                    }
                    case EVENT -> {
                        command.rejectUnexpectedOptions("/from", "/to");
                        String from = command.getRequiredOption("/from");
                        String to = command.getRequiredOption("/to");
                        addTask(tasks, new Event(
                                command.getArgument(),
                                from,
                                to));
                    }
                    }
                } catch (ZuccException exception) {
                    printResponse(exception.getMessage());
                }
            }
        }
    }
}
