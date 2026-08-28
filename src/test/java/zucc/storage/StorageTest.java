package zucc.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import zucc.ZuccException;
import zucc.task.Deadline;
import zucc.task.Event;
import zucc.task.Task;
import zucc.task.TaskList;
import zucc.task.Todo;

/**
 * Tests task persistence using isolated temporary files.
 */
public class StorageTest {
    @TempDir
    private Path temporaryDirectory;

    @Test
    public void loadTasks_fileDoesNotExist_emptyListReturned() throws ZuccException {
        Path taskFile = temporaryDirectory.resolve("missing.txt");

        List<Task> loadedTasks = new Storage(taskFile).loadTasks();

        assertTrue(loadedTasks.isEmpty());
    }

    @Test
    public void saveAndLoadTasks_validTasks_orderDataAndStatusPreserved()
            throws ZuccException {
        Path taskFile = temporaryDirectory.resolve("nested/data/tasks.txt");
        Storage storage = new Storage(taskFile);
        Deadline deadline = new Deadline("Submit | report", "2/9/2026 1800");
        deadline.markAsDone();
        TaskList originalTasks = new TaskList(List.of(
                new Todo("Read chapter"),
                deadline,
                new Event("Workshop", "3/9/2026 0900", "4/9/2026 1700")));

        storage.saveTasks(originalTasks);
        List<Task> loadedTasks = storage.loadTasks();

        assertEquals(
                List.of(
                        "T | 0 | Read chapter",
                        "D | 1 | Submit %7C report | 2/9/2026 1800",
                        "E | 0 | Workshop | 3/9/2026 0900 | 4/9/2026 1700"),
                loadedTasks.stream().map(Task::toStorageString).toList());
    }

    @Test
    public void saveTasks_parentDirectoriesMissing_directoriesAndFileCreated()
            throws ZuccException, IOException {
        Path taskFile = temporaryDirectory.resolve("one/two/tasks.txt");
        Storage storage = new Storage(taskFile);

        storage.saveTasks(List.of(new Todo("Read chapter")));

        assertEquals(
                List.of("T | 0 | Read chapter"),
                Files.readAllLines(taskFile, StandardCharsets.UTF_8));
    }

    @Test
    public void saveTasks_emptyTaskList_existingFileCleared()
            throws ZuccException, IOException {
        Path taskFile = temporaryDirectory.resolve("tasks.txt");
        Files.writeString(
                taskFile,
                "T | 0 | Stale task",
                StandardCharsets.UTF_8);
        Storage storage = new Storage(taskFile);

        storage.saveTasks(List.of());

        assertTrue(Files.readAllLines(taskFile, StandardCharsets.UTF_8).isEmpty());
        assertTrue(storage.loadTasks().isEmpty());
    }

    @Test
    public void saveAndLoadTasks_unicodeDescription_textPreserved()
            throws ZuccException {
        Path taskFile = temporaryDirectory.resolve("tasks.txt");
        Storage storage = new Storage(taskFile);

        storage.saveTasks(List.of(new Todo("Réviser 日本語 🚀")));
        List<Task> loadedTasks = storage.loadTasks();

        assertEquals("[T][ ] Réviser 日本語 🚀", loadedTasks.getFirst().toString());
    }

    @Test
    public void loadTasks_invalidSecondLine_exceptionIdentifiesLineAndCause()
            throws IOException {
        Path taskFile = temporaryDirectory.resolve("tasks.txt");
        Files.write(
                taskFile,
                List.of("T | 0 | Read chapter", "X | 0 | invalid task"),
                StandardCharsets.UTF_8);
        Storage storage = new Storage(taskFile);

        ZuccException exception = assertThrows(ZuccException.class, storage::loadTasks);

        assertEquals(
                "Zucc couldn't load tasks because line 2 of "
                        + taskFile + " is invalid.",
                exception.getMessage());
        assertInstanceOf(ZuccException.class, exception.getCause());
    }

    @Test
    public void loadTasks_pathIsDirectory_exceptionThrown() {
        Storage storage = new Storage(temporaryDirectory);

        ZuccException exception = assertThrows(ZuccException.class, storage::loadTasks);

        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    public void saveTasks_pathIsDirectory_exceptionThrown() throws ZuccException {
        Storage storage = new Storage(temporaryDirectory);

        ZuccException exception = assertThrows(
                ZuccException.class,
                () -> storage.saveTasks(List.of(new Todo("Read chapter"))));

        assertInstanceOf(IOException.class, exception.getCause());
    }
}
