package zucc.task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import zucc.ZuccException;

/**
 * Owns the collection of tasks and provides operations that act on that collection.
 * Task numbers shown to users are one-based, while methods that select a task use
 * zero-based indexes supplied by the command-handling layer.
 */
public final class TaskList implements Iterable<Task> {
    /** Error shown when a task index does not identify a stored task. */
    private static final String TASK_NOT_FOUND_ERROR =
            "Zucc couldn't find that task in the records. Use list to check its number.";

    /** Tasks in their display and storage order. */
    private final List<Task> tasks;

    /**
     * Creates a task list containing the supplied tasks.
     * A defensive copy ensures that this class remains the owner of its collection.
     *
     * @param initialTasks tasks with which to initialize the list.
     */
    public TaskList(List<Task> initialTasks) {
        tasks = new ArrayList<>(initialTasks);
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param newTask task to add.
     */
    public void add(Task newTask) {
        tasks.add(newTask);
    }

    /**
     * Removes and returns the task at a zero-based index.
     *
     * @param taskIndex zero-based index of the task to remove.
     * @return removed task.
     * @throws ZuccException if the index is outside the task list.
     */
    public Task delete(int taskIndex) throws ZuccException {
        Task removedTask = getTask(taskIndex);
        tasks.remove(taskIndex);
        return removedTask;
    }

    /**
     * Marks the task at a zero-based index as done.
     *
     * @param taskIndex zero-based index of the task to mark.
     * @return updated task.
     * @throws ZuccException if the index is invalid or the task is already done.
     */
    public Task mark(int taskIndex) throws ZuccException {
        Task task = getTask(taskIndex);
        task.markAsDone();
        return task;
    }

    /**
     * Marks the task at a zero-based index as incomplete.
     *
     * @param taskIndex zero-based index of the task to unmark.
     * @return updated task.
     * @throws ZuccException if the index is invalid or the task is already incomplete.
     */
    public Task unmark(int taskIndex) throws ZuccException {
        Task task = getTask(taskIndex);
        task.markAsNotDone();
        return task;
    }

    /**
     * Returns the number of tasks in the list.
     *
     * @return current task count.
     */
    public int getTaskCount() {
        return tasks.size();
    }

    /**
     * Provides structurally read-only traversal of the tasks in their current order.
     * The returned iterator does not support removing tasks, so structural
     * changes must go through this class's task operations.
     *
     * @return iterator over the tasks.
     */
    @Override
    public Iterator<Task> iterator() {
        return Collections.unmodifiableList(tasks).iterator();
    }

    /**
     * Formats scheduled tasks occurring on a date using their original task numbers.
     * Preserving those numbers lets users immediately mark or delete a matching task.
     *
     * @param date date for which tasks should be shown.
     * @return matching tasks, one per line.
     */
    public String formatTasksOn(LocalDate date) {
        return formatTasksMatching(task -> task.occursOn(date));
    }

    /**
     * Formats tasks whose descriptions contain a keyword, preserving their
     * original task numbers.
     *
     * @param keyword text to search for in task descriptions.
     * @return matching tasks, one per line.
     */
    public String formatTasksContaining(String keyword) {
        return formatTasksMatching(task -> task.descriptionContains(keyword));
    }

    /**
     * Returns a task after ensuring its zero-based index is available.
     *
     * @param taskIndex zero-based task index.
     * @return task at the requested index.
     * @throws ZuccException if the index is outside the task list.
     */
    private Task getTask(int taskIndex) throws ZuccException {
        if (taskIndex < 0 || taskIndex >= tasks.size()) {
            throw new ZuccException(TASK_NOT_FOUND_ERROR);
        }
        return tasks.get(taskIndex);
    }

    /**
     * Formats tasks matching a condition while preserving their original task numbers.
     *
     * @param condition condition a task must satisfy to be included.
     * @return matching tasks, one per line.
     */
    private String formatTasksMatching(Predicate<Task> condition) {
        StringBuilder taskList = new StringBuilder();

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (!condition.test(task)) {
                continue;
            }
            if (!taskList.isEmpty()) {
                taskList.append('\n');
            }
            taskList.append(i + 1)
                    .append('.')
                    .append(task);
        }

        return taskList.toString();
    }

    /**
     * Formats all tasks as a one-based numbered list.
     *
     * @return all tasks, one per line.
     */
    @Override
    public String toString() {
        return formatTasksMatching(task -> true);
    }
}
