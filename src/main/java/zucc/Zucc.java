package zucc;

import java.nio.file.Path;

import zucc.command.Command;
import zucc.storage.Storage;
import zucc.task.TaskList;
import zucc.ui.Ui;

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
     * @param taskFilePath file from which tasks are loaded and to which they are saved.
     * @throws ZuccException if existing task data cannot be loaded
     */
    public Zucc(Path taskFilePath) throws ZuccException {
        storage = new Storage(taskFilePath);
        tasks = new TaskList(storage.loadTasks());
    }

    /**
     * Greets the user and handles commands until input ends or the user enters {@code bye}.
     *
     * @param ui user interface for this interactive session.
     */
    public void run(Ui ui) {
        ui.showGreeting();

        boolean isExit = false;
        while (!isExit && ui.hasNextCommand()) {
            try {
                Command command = Command.parse(ui.readCommand());
                command.execute(tasks, ui, storage);
                isExit = command.isExit();
            } catch (ZuccException exception) {
                ui.showMessage(exception.getMessage());
            }
        }
    }

    /**
     * Creates the application's resources and starts Zucc.
     *
     * @param args command-line arguments; not used by this application.
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
