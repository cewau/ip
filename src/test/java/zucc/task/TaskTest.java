package zucc.task;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import zucc.ZuccException;

/**
 * Tests task state transitions and the shared persistence format.
 */
public class TaskTest {
    private Todo todo;

    @BeforeEach
    public void setUp() throws ZuccException {
        todo = new Todo("Read testing chapter");
    }

    @Test
    public void markAsDone_incompleteTask_statusChangedToDone() throws ZuccException {
        todo.markAsDone();

        assertEquals("X", todo.getStatusIcon());
    }

    @Test
    public void markAsDone_completedTask_exceptionThrown() throws ZuccException {
        todo.markAsDone();

        assertThrows(ZuccException.class, todo::markAsDone);
    }

    @Test
    public void markAsNotDone_completedTask_statusChangedToIncomplete() throws ZuccException {
        todo.markAsDone();

        todo.markAsNotDone();

        assertEquals(" ", todo.getStatusIcon());
    }

    @Test
    public void markAsNotDone_incompleteTask_exceptionThrown() {
        assertThrows(ZuccException.class, todo::markAsNotDone);
    }

    @Test
    public void toStorageString_specialStorageCharacters_charactersEscaped()
            throws ZuccException {
        Todo task = new Todo("Compare A | B at 100% confidence");
        task.markAsDone();

        assertEquals(
                "T | 1 | Compare A %7C B at 100%25 confidence",
                task.toStorageString());
    }

    @Test
    public void storageRoundTrip_eachTaskType_dataAndStatusPreserved() throws ZuccException {
        Todo todoTask = new Todo("Use literal %7C and | symbols");
        Deadline deadlineTask = new Deadline("Submit report", "2/9/2026 1800");
        Event eventTask = new Event(
                "Attend workshop", "3/9/2026 0900", "4/9/2026 1700");
        deadlineTask.markAsDone();

        assertAll(
                () -> assertStorageRoundTrip(todoTask, Todo.class),
                () -> assertStorageRoundTrip(deadlineTask, Deadline.class),
                () -> assertStorageRoundTrip(eventTask, Event.class));
    }

    @Test
    public void fromStorageString_unknownType_exceptionThrown() {
        assertThrows(ZuccException.class,
                () -> Task.fromStorageString("X | 0 | unsupported"));
    }

    @Test
    public void fromStorageString_invalidFieldCountOrStatus_exceptionThrown() {
        assertAll(
                () -> assertThrows(ZuccException.class,
                        () -> Task.fromStorageString("T | 0")),
                () -> assertThrows(ZuccException.class,
                        () -> Task.fromStorageString("D | maybe | submit | 2/9/2026 1800")),
                () -> assertThrows(ZuccException.class,
                        () -> Task.fromStorageString(
                                "E | 0 | workshop | 3/9/2026 0900")));
    }

    /**
     * Verifies that saving and loading one task preserves its concrete type and data.
     *
     * @param original task to save and load
     * @param expectedType concrete type expected after loading
     * @throws ZuccException if the stored representation cannot be loaded
     */
    private void assertStorageRoundTrip(Task original, Class<? extends Task> expectedType)
            throws ZuccException {
        Task restored = Task.fromStorageString(original.toStorageString());

        assertInstanceOf(expectedType, restored);
        assertEquals(original.toStorageString(), restored.toStorageString());
        assertEquals(original.toString(), restored.toString());
    }
}
