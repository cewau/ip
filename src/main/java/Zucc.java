import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Coordinates Zucc's task state, persistence, parsing, and user interface.
 */
public class Zucc {
    /** File used to preserve tasks between application runs. */
    private static final Path TASK_FILE_PATH = Path.of("data", "zucc.txt");

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
     * @param newTask task to add
     * @return confirmation describing the added task and updated task count
     * @throws ZuccException if the updated list cannot be saved
     */
    private String addTaskAndCreateResponse(Task newTask) throws ZuccException {
        addTask(newTask);
        return "Got it. I've added this task:\n  "
                + newTask
                + "\nNow you have " + getTaskCount() + " tasks in the list.";
    }

    /**
     * Greets the user and handles commands until input ends or the user enters {@code bye}.
     *
     * @param ui user interface for this interactive session
     */
    public void run(Ui ui) {
        ui.showGreeting();

        while (ui.hasNextCommand()) {
            try {
                ParsedCommand command = Parser.parse(ui.readCommand());

                switch (command.getType()) {
                case BYE -> {
                    command.requireNoArguments();
                    ui.showMessage("Bye. Hope to see you again soon!");
                    return;
                }
                case LIST -> {
                    command.requireNoArguments();
                    ui.showMessage("Here are the tasks in your list:\n" + tasks);
                }
                case ON -> {
                    command.rejectUnexpectedOptions();
                    LocalDate date = TaskDateTimeFormat.parseDate(command.getArgument());
                    String displayDate = TaskDateTimeFormat.formatDateForDisplay(date);
                    String matchingTasks = tasks.formatTasksOn(date);
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
                    int taskIndex = Parser.parseTaskIndex(command.getArgument());
                    Task removedTask = deleteTask(taskIndex);
                    ui.showMessage("Noted. I've removed this task:\n  "
                            + removedTask
                            + "\nNow you have " + getTaskCount()
                            + " tasks in the list.");
                }
                case MARK -> {
                    command.rejectUnexpectedOptions();
                    int taskIndex = Parser.parseTaskIndex(command.getArgument());
                    Task markedTask = markTask(taskIndex);
                    ui.showMessage("Nice! I've marked this task as done:\n  "
                            + markedTask);
                }
                case UNMARK -> {
                    command.rejectUnexpectedOptions();
                    int taskIndex = Parser.parseTaskIndex(command.getArgument());
                    Task unmarkedTask = unmarkTask(taskIndex);
                    ui.showMessage("OK, I've marked this task as not done yet:\n  "
                            + unmarkedTask);
                }
                case TODO -> {
                    command.rejectUnexpectedOptions();
                    ui.showMessage(addTaskAndCreateResponse(
                            new Todo(command.getArgument())));
                }
                case DEADLINE -> {
                    command.rejectUnexpectedOptions("/by");
                    String by = command.getRequiredOption("/by");
                    ui.showMessage(addTaskAndCreateResponse(
                            new Deadline(command.getArgument(), by)));
                }
                case EVENT -> {
                    command.rejectUnexpectedOptions("/from", "/to");
                    String from = command.getRequiredOption("/from");
                    String to = command.getRequiredOption("/to");
                    ui.showMessage(addTaskAndCreateResponse(
                            new Event(command.getArgument(), from, to)));
                }
                }
            } catch (ZuccException exception) {
                ui.showMessage(exception.getMessage());
            }
        }
    }

    /**
     * Creates the application's resources and starts Zucc.
     *
     * @param args command-line arguments; not used by this application
     */
    public static void main(String[] args) {
        try (Ui ui = new Ui()) {
            try {
                new Zucc(TASK_FILE_PATH).run(ui);
            } catch (ZuccException exception) {
                ui.showMessage(exception.getMessage());
            }
        }
    }
}
