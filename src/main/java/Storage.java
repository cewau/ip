import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and saves Zucc's tasks in a small, readable text file.
 * Each line stores a type, completion flag, and the fields belonging to one task.
 */
public final class Storage {
    /** Location of the task data file, relative to the working directory. */
    private final Path filePath;

    /**
     * Creates storage backed by the given file.
     *
     * @param filePath location at which tasks are stored
     */
    public Storage(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads all tasks from disk, or returns an empty list when no data file exists yet.
     *
     * @return tasks in the same order in which they were saved
     * @throws ZuccException if the file cannot be read or contains invalid task data
     */
    public List<Task> loadTasks() throws ZuccException {
        if (Files.notExists(filePath)) {
            return new ArrayList<>();
        }

        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            List<Task> tasks = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                try {
                    tasks.add(Task.fromStorageString(lines.get(i)));
                } catch (ZuccException exception) {
                    throw new ZuccException("Zucc couldn't load tasks because line "
                            + (i + 1) + " of " + filePath + " is invalid.", exception);
                }
            }
            return tasks;
        } catch (IOException exception) {
            throw new ZuccException("Zucc couldn't load tasks from " + filePath
                    + ". Check that the file is readable.", exception);
        }
    }

    /**
     * Writes the complete current task list to disk, creating its directory if needed.
     *
     * @param tasks tasks to save in their current order
     * @throws ZuccException if the data file cannot be written
     */
    public void saveTasks(Iterable<Task> tasks) throws ZuccException {
        List<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            lines.add(task.toStorageString());
        }

        try {
            Path parentDirectory = filePath.getParent();
            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new ZuccException("Zucc couldn't save tasks to " + filePath
                    + ". Check that the location is writable.", exception);
        }
    }

}
