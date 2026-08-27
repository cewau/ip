import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Owns Zucc's persistent task state and starts the command-line interface.
 */
public class Zucc {
    /** File used to preserve tasks between application runs. */
    private static final Path TASK_FILE_PATH = Path.of("data", "zucc.txt");

    /** Error shown when a task number is missing or malformed. */
    private static final String INVALID_TASK_NUMBER_ERROR =
            "Zucc couldn't find that task in the records. Use list to check its number.";

    /** Tasks in the current chatbot session. */
    private final TaskList tasks;

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
        tasks = new TaskList(storage.loadTasks());
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
        throw new ZuccException(INVALID_TASK_NUMBER_ERROR);
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
        Task removedTask = tasks.delete(taskIndex);
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
        Task task = tasks.mark(taskIndex);
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
        Task task = tasks.unmark(taskIndex);
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
     * Adds a task through the stateful chatbot and creates the CLI confirmation.
     *
     * @param zucc chatbot instance that owns the task state
     * @param newTask task to add
     * @return confirmation describing the added task and updated task count
     * @throws ZuccException if the updated list cannot be saved
     */
    private static String addTaskAndCreateResponse(Zucc zucc, Task newTask)
            throws ZuccException {
        zucc.addTask(newTask);
        return "Got it. I've added this task:\n  "
                + newTask
                + "\nNow you have " + zucc.getTaskCount() + " tasks in the list.";
    }

    /**
     * Greets the user, stores tasks in a collection, lists, deletes, or updates
     * their completion status on request, and exits when the user enters {@code bye}.
     *
     * @param args command-line arguments; not used by this application
     */
    public static void main(String[] args) {
        try (Ui ui = new Ui()) {
            Zucc zucc;
            try {
                zucc = new Zucc(TASK_FILE_PATH);
            } catch (ZuccException exception) {
                ui.showMessage(exception.getMessage());
                return;
            }

            ui.showGreeting();

            while (ui.hasNextCommand()) {
                try {
                    ParsedCommand command = new ParsedCommand(ui.readCommand());

                    switch (command.getType()) {
                    case BYE -> {
                        command.requireNoArguments();
                        ui.showMessage("Bye. Hope to see you again soon!");
                        return;
                    }
                    case LIST -> {
                        command.requireNoArguments();
                        ui.showMessage("Here are the tasks in your list:\n" + zucc.tasks);
                    }
                    case ON -> {
                        command.rejectUnexpectedOptions();
                        LocalDate date = TaskDateTimeFormat.parseDate(command.getArgument());
                        String displayDate = TaskDateTimeFormat.formatDateForDisplay(date);
                        String matchingTasks = zucc.tasks.formatTasksOn(date);
                        if (matchingTasks.isEmpty()) {
                            ui.showMessage("Zucc scanned the timeline and found nothing on "
                                    + displayDate + ". Suspiciously peaceful.");
                        } else {
                            ui.showMessage("Here are the tasks on " + displayDate + ":\n"
                                    + matchingTasks);
                        }
                    }
                    case DELETE -> {
                        command.rejectUnexpectedOptions();
                        int taskIndex = parseTaskIndex(command.getArgument());
                        Task removedTask = zucc.deleteTask(taskIndex);
                        ui.showMessage("Noted. I've removed this task:\n  "
                                + removedTask
                                + "\nNow you have " + zucc.getTaskCount()
                                + " tasks in the list.");
                    }
                    case MARK -> {
                        command.rejectUnexpectedOptions();
                        int taskIndex = parseTaskIndex(command.getArgument());
                        Task markedTask = zucc.markTask(taskIndex);
                        ui.showMessage("Nice! I've marked this task as done:\n  "
                                + markedTask);
                    }
                    case UNMARK -> {
                        command.rejectUnexpectedOptions();
                        int taskIndex = parseTaskIndex(command.getArgument());
                        Task unmarkedTask = zucc.unmarkTask(taskIndex);
                        ui.showMessage("OK, I've marked this task as not done yet:\n  "
                                + unmarkedTask);
                    }
                    case TODO -> {
                        command.rejectUnexpectedOptions();
                        ui.showMessage(addTaskAndCreateResponse(
                                zucc, new Todo(command.getArgument())));
                    }
                    case DEADLINE -> {
                        command.rejectUnexpectedOptions("/by");
                        String by = command.getRequiredOption("/by");
                        ui.showMessage(addTaskAndCreateResponse(
                                zucc, new Deadline(command.getArgument(), by)));
                    }
                    case EVENT -> {
                        command.rejectUnexpectedOptions("/from", "/to");
                        String from = command.getRequiredOption("/from");
                        String to = command.getRequiredOption("/to");
                        ui.showMessage(addTaskAndCreateResponse(
                                zucc, new Event(command.getArgument(), from, to)));
                    }
                    }
                } catch (ZuccException exception) {
                    ui.showMessage(exception.getMessage());
                }
            }
        }
    }
}
