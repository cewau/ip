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
                String command = scanner.nextLine();

                if (command.equals("bye")) {
                    printResponse("Bye. Hope to see you again soon!");
                    break;
                }

                if (command.equals("list")) {
                    String taskList = formatTasks(tasks);
                    printResponse("Here are the tasks in your list:\n" + taskList);
                    continue;
                }

                if (command.equals("delete") || command.startsWith("delete ")) {
                    try {
                        String taskNumberText = command.substring("delete".length()).trim();
                        int taskIndex = parseTaskIndex(taskNumberText, tasks.size());
                        Task removedTask = tasks.remove(taskIndex);
                        printResponse("Noted. I've removed this task:\n  "
                                + removedTask
                                + "\nNow you have " + tasks.size() + " tasks in the list.");
                    } catch (ZuccException exception) {
                        printResponse(exception.getMessage());
                    }
                    continue;
                }

                if (command.equals("mark") || command.startsWith("mark ")) {
                    try {
                        String taskNumberText = command.substring("mark".length()).trim();
                        int taskIndex = parseTaskIndex(taskNumberText, tasks.size());
                        tasks.get(taskIndex).markAsDone();
                        printResponse("Nice! I've marked this task as done:\n  "
                                + tasks.get(taskIndex));
                    } catch (ZuccException exception) {
                        printResponse(exception.getMessage());
                    }
                    continue;
                }

                if (command.equals("unmark") || command.startsWith("unmark ")) {
                    try {
                        String taskNumberText = command.substring("unmark".length()).trim();
                        int taskIndex = parseTaskIndex(taskNumberText, tasks.size());
                        tasks.get(taskIndex).markAsNotDone();
                        printResponse("OK, I've marked this task as not done yet:\n  "
                                + tasks.get(taskIndex));
                    } catch (ZuccException exception) {
                        printResponse(exception.getMessage());
                    }
                    continue;
                }

                Task newTask = null;

                // TODO: Revisit shared option parsing if these command formats remain aligned.
                // Parsing stays command-specific for now so future date/time formats can diverge.
                if (command.equals("todo") || command.startsWith("todo ")) {
                    String description = command.substring("todo".length()).trim();
                    if (description.isBlank()) {
                        printResponse("Zucc needs more data: give that todo a description.");
                        continue;
                    }
                    newTask = new Todo(description);
                } else if (command.equals("deadline") || command.startsWith("deadline ")) {
                    String deadlineDetails = command.substring("deadline".length()).trim();
                    String[] deadlineParts = deadlineDetails.split("\\s+/by(?:\\s+|$)", 2);
                    if (deadlineParts.length < 2
                            || deadlineParts[0].isBlank()
                            || deadlineParts[1].isBlank()) {
                        printResponse("Zucc needs more data: add a deadline description "
                                + "followed by /by and a due date.");
                        continue;
                    }
                    String description = deadlineParts[0].trim();
                    String by = deadlineParts[1].trim();
                    newTask = new Deadline(description, by);
                } else if (command.equals("event") || command.startsWith("event ")) {
                    String eventDetails = command.substring("event".length()).trim();
                    String eventFormatError = "Zucc needs more data: add an event description "
                            + "followed by /from and /to times.";
                    String[] fromParts = eventDetails.split("\\s+/from(?:\\s+|$)", 2);
                    if (fromParts.length < 2) {
                        printResponse(eventFormatError);
                        continue;
                    }

                    String[] toParts = fromParts[1].split("\\s+/to(?:\\s+|$)", 2);
                    if (toParts.length < 2
                            || fromParts[0].isBlank()
                            || toParts[0].isBlank()
                            || toParts[1].isBlank()) {
                        printResponse(eventFormatError);
                        continue;
                    }

                    String description = fromParts[0].trim();
                    String from = toParts[0].trim();
                    String to = toParts[1].trim();
                    newTask = new Event(description, from, to);
                }

                if (newTask != null) {
                    tasks.add(newTask);
                    printResponse("Got it. I've added this task:\n  "
                            + newTask
                            + "\nNow you have " + tasks.size() + " tasks in the list.");
                    continue;
                }

                printResponse("Zucc's algorithm doesn't recognize that command. "
                        + "Try todo, deadline, event, list, mark, unmark, delete, or bye.");
            }
        }
    }
}
