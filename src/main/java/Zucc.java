import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/**
 * Owns Zucc's persistent task state and starts the command-line interface.
 */
public class Zucc {
    /** A visual separator used to frame the chatbot's messages. */
    private static final String SEPARATOR = "____________________________________________________________";

    /** The number of spaces before each separator. */
    private static final int SEPARATOR_INDENT = 4;

    /** Messages are indented one space farther than their separators. */
    private static final int MESSAGE_INDENT = SEPARATOR_INDENT + 1;

    /** File used to preserve tasks between application runs. */
    private static final Path TASK_FILE_PATH = Path.of("data", "zucc.txt");

    /** Error shown when a task index does not identify a stored task. */
    private static final String TASK_NOT_FOUND_ERROR =
            "Zucc couldn't find that task in the records. Use list to check its number.";

    /** Tasks in the current chatbot session. */
    private final List<Task> tasks;

    /** Persistent storage updated whenever the task state changes. */
    private final Storage storage;

    /**
     * Creates a chatbot whose state is backed by the given data file.
     *
     * @param taskFilePath file from which tasks are loaded and to which they are saved
     * @throws ZuccException if existing task data cannot be loaded
     */
    public Zucc(Path taskFilePath) throws ZuccException {
        storage = new Storage(taskFilePath);
        tasks = storage.loadTasks();
    }

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
     * @return all stored tasks, one per line
     */
    @Override
    public String toString() {
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
     * @return the corresponding zero-based array index
     * @throws ZuccException if the number is missing or malformed
     */
    private static int parseTaskIndex(String taskNumberText) throws ZuccException {
        try {
            return Integer.parseInt(taskNumberText) - 1;
        } catch (NumberFormatException ignored) {
            // Malformed input and unavailable task numbers use the same response.
        }
        throw new ZuccException(TASK_NOT_FOUND_ERROR);
    }

    /**
     * Returns a task after ensuring its zero-based index is available.
     *
     * @param taskIndex zero-based task index
     * @return task at the requested index
     * @throws ZuccException if the index is outside the current task list
     */
    private Task getTask(int taskIndex) throws ZuccException {
        if (taskIndex < 0 || taskIndex >= tasks.size()) {
            throw new ZuccException(TASK_NOT_FOUND_ERROR);
        }
        return tasks.get(taskIndex);
    }

    /**
     * Adds a task and immediately persists the updated list.
     *
     * @param newTask task to add
     * @throws ZuccException if the updated list cannot be saved
     */
    public void addTask(Task newTask) throws ZuccException {
        tasks.add(newTask);
        storage.saveTasks(tasks);
    }

    /**
     * Removes a numbered task and immediately persists the updated list.
     *
     * @param taskIndex zero-based index of the task to remove
     * @return removed task
     * @throws ZuccException if the index is invalid or the list cannot be saved
     */
    public Task deleteTask(int taskIndex) throws ZuccException {
        Task removedTask = getTask(taskIndex);
        tasks.remove(taskIndex);
        storage.saveTasks(tasks);
        return removedTask;
    }

    /**
     * Marks a numbered task as done and persists its new state.
     *
     * @param taskIndex zero-based index of the task to mark
     * @return updated task
     * @throws ZuccException if the index is invalid or the task cannot be marked or saved
     */
    public Task markTask(int taskIndex) throws ZuccException {
        Task task = getTask(taskIndex);
        task.markAsDone();
        storage.saveTasks(tasks);
        return task;
    }

    /**
     * Marks a numbered task as incomplete and persists its new state.
     *
     * @param taskIndex zero-based index of the task to unmark
     * @return updated task
     * @throws ZuccException if the index is invalid or the task cannot be unmarked or saved
     */
    public Task unmarkTask(int taskIndex) throws ZuccException {
        Task task = getTask(taskIndex);
        task.markAsNotDone();
        storage.saveTasks(tasks);
        return task;
    }

    /**
     * Returns the number of tasks currently managed by this chatbot.
     *
     * @return current task count
     */
    public int getTaskCount() {
        return tasks.size();
    }

    /**
     * Adds a task through the stateful chatbot and prints the CLI confirmation.
     *
     * @param zucc chatbot instance that owns the task state
     * @param newTask task to add
     * @throws ZuccException if the updated list cannot be saved
     */
    private static void addTaskAndRespond(Zucc zucc, Task newTask) throws ZuccException {
        zucc.addTask(newTask);
        printResponse("Got it. I've added this task:\n  "
                + newTask
                + "\nNow you have " + zucc.getTaskCount() + " tasks in the list.");
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

        Zucc zucc;
        try {
            zucc = new Zucc(TASK_FILE_PATH);
        } catch (ZuccException exception) {
            printResponse(exception.getMessage());
            return;
        }

        printResponse(greeting);

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
                        printResponse("Here are the tasks in your list:\n" + zucc);
                    }
                    case DELETE -> {
                        command.rejectUnexpectedOptions();
                        int taskIndex = parseTaskIndex(command.getArgument());
                        Task removedTask = zucc.deleteTask(taskIndex);
                        printResponse("Noted. I've removed this task:\n  "
                                + removedTask
                                + "\nNow you have " + zucc.getTaskCount()
                                + " tasks in the list.");
                    }
                    case MARK -> {
                        command.rejectUnexpectedOptions();
                        int taskIndex = parseTaskIndex(command.getArgument());
                        Task markedTask = zucc.markTask(taskIndex);
                        printResponse("Nice! I've marked this task as done:\n  "
                                + markedTask);
                    }
                    case UNMARK -> {
                        command.rejectUnexpectedOptions();
                        int taskIndex = parseTaskIndex(command.getArgument());
                        Task unmarkedTask = zucc.unmarkTask(taskIndex);
                        printResponse("OK, I've marked this task as not done yet:\n  "
                                + unmarkedTask);
                    }
                    case TODO -> {
                        command.rejectUnexpectedOptions();
                        addTaskAndRespond(zucc, new Todo(command.getArgument()));
                    }
                    case DEADLINE -> {
                        command.rejectUnexpectedOptions("/by");
                        String by = command.getRequiredOption("/by");
                        addTaskAndRespond(zucc, new Deadline(command.getArgument(), by));
                    }
                    case EVENT -> {
                        command.rejectUnexpectedOptions("/from", "/to");
                        String from = command.getRequiredOption("/from");
                        String to = command.getRequiredOption("/to");
                        addTaskAndRespond(zucc, new Event(
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
