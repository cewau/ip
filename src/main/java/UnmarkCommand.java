/**
 * Marks the task identified by the command's raw task-number argument as incomplete.
 */
public final class UnmarkCommand extends Command {
    /** Creates an unmark command awaiting values from Parser. */
    UnmarkCommand() {
        super("unmark");
    }

    /**
     * Validates the task number before unmarking and persisting the selected task.
     *
     * @param tasks task collection to update
     * @param ui user interface through which confirmation is shown
     * @param storage persistent storage to update
     * @throws ZuccException if the task cannot be unmarked or persisted
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws ZuccException {
        int taskIndex = parseTaskIndex();
        Task unmarkedTask = tasks.unmark(taskIndex);
        storage.saveTasks(tasks);
        ui.showMessage("OK, I've marked this task as not done yet:\n  " + unmarkedTask);
    }
}
