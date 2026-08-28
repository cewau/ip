package zucc.command;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import zucc.ZuccException;
import zucc.task.Task;

/**
 * Tests command parsing and the validation shared by concrete commands.
 */
public class CommandTest {
    @Test
    public void parse_knownKeywords_returnsMatchingCommandTypes() {
        assertAll(
                () -> assertInstanceOf(TodoCommand.class, Command.parse("todo read")),
                () -> assertInstanceOf(DeadlineCommand.class,
                        Command.parse("deadline submit /by 1/9/2026 1800")),
                () -> assertInstanceOf(EventCommand.class,
                        Command.parse("event meeting /from 1/9/2026 1800 /to 1/9/2026 1900")),
                () -> assertInstanceOf(ListCommand.class, Command.parse("list")),
                () -> assertInstanceOf(FindCommand.class, Command.parse("find book")),
                () -> assertInstanceOf(OnCommand.class, Command.parse("on 1/9/2026")),
                () -> assertInstanceOf(MarkCommand.class, Command.parse("mark 1")),
                () -> assertInstanceOf(UnmarkCommand.class, Command.parse("unmark 1")),
                () -> assertInstanceOf(DeleteCommand.class, Command.parse("delete 1")),
                () -> assertInstanceOf(ExitCommand.class, Command.parse("bye")));
    }

    @Test
    public void parse_unknownOrBlankCommand_exceptionThrown() {
        assertAll(
                () -> assertThrows(ZuccException.class, () -> Command.parse("archive 1")),
                () -> assertThrows(ZuccException.class, () -> Command.parse("   ")));
    }

    @Test
    public void parse_todoWithRepeatedSpaces_spacesPreservedInDescription()
            throws ZuccException {
        TodoCommand command = assertInstanceOf(
                TodoCommand.class, Command.parse("  todo read  chapter  "));

        assertEquals("[T][ ] read  chapter", command.createTask().toString());
    }

    @Test
    public void parse_slashInsideArgumentWord_slashPreservedInDescription()
            throws ZuccException {
        TodoCommand command = assertInstanceOf(
                TodoCommand.class,
                Command.parse("todo review input/output handling"));

        assertEquals(
                "[T][ ] review input/output handling",
                command.createTask().toString());
    }

    @Test
    public void parse_deadlineWithOption_taskCreated() throws ZuccException {
        DeadlineCommand command = assertInstanceOf(
                DeadlineCommand.class,
                Command.parse("deadline submit report /by 2/9/2026 1800"));

        Task task = command.createTask();

        assertEquals("[D][ ] submit report (by: Sep 02 2026, 6:00PM)", task.toString());
    }

    @Test
    public void parse_eventOptionsInEitherOrder_tasksCreated() throws ZuccException {
        EventCommand usualOrder = assertInstanceOf(
                EventCommand.class,
                Command.parse("event workshop /from 2/9/2026 1800 /to 2/9/2026 2000"));
        EventCommand reversedOrder = assertInstanceOf(
                EventCommand.class,
                Command.parse("event workshop /to 2/9/2026 2000 /from 2/9/2026 1800"));

        assertEquals(usualOrder.createTask().toString(), reversedOrder.createTask().toString());
    }

    @Test
    public void parse_duplicateOption_exceptionThrown() {
        assertThrows(ZuccException.class,
                () -> Command.parse(
                        "deadline submit /by 2/9/2026 1800 /by 3/9/2026 1800"));
    }

    @Test
    public void parse_optionUnsupportedByCommand_exceptionThrown() {
        assertThrows(ZuccException.class,
                () -> Command.parse("todo read /by 2/9/2026 1800"));
    }

    @Test
    public void createTask_requiredArgumentOrOptionMissing_exceptionThrown()
            throws ZuccException {
        TodoCommand missingDescription = assertInstanceOf(
                TodoCommand.class, Command.parse("todo"));
        DeadlineCommand missingDeadline = assertInstanceOf(
                DeadlineCommand.class, Command.parse("deadline submit"));
        EventCommand missingEnd = assertInstanceOf(
                EventCommand.class,
                Command.parse("event workshop /from 2/9/2026 1800"));

        assertAll(
                () -> assertThrows(ZuccException.class, missingDescription::createTask),
                () -> assertThrows(ZuccException.class, missingDeadline::createTask),
                () -> assertThrows(ZuccException.class, missingEnd::createTask));
    }

    @Test
    public void parseTaskIndex_validOneBasedNumber_returnsZeroBasedIndex()
            throws ZuccException {
        MarkCommand command = assertInstanceOf(MarkCommand.class, Command.parse("mark 3"));

        assertEquals(2, command.parseTaskIndex());
    }

    @Test
    public void parseTaskIndex_missingMalformedOrTooLargeNumber_exceptionThrown()
            throws ZuccException {
        MarkCommand missing = assertInstanceOf(MarkCommand.class, Command.parse("mark"));
        MarkCommand malformed = assertInstanceOf(MarkCommand.class, Command.parse("mark two"));
        MarkCommand tooLargeForInteger = assertInstanceOf(
                MarkCommand.class, Command.parse("mark 999999999999999999999"));

        assertAll(
                () -> assertThrows(ZuccException.class, missing::parseTaskIndex),
                () -> assertThrows(ZuccException.class, malformed::parseTaskIndex),
                () -> assertThrows(ZuccException.class, tooLargeForInteger::parseTaskIndex));
    }

    @Test
    public void requireNoArgument_argumentSupplied_exceptionThrown() throws ZuccException {
        ListCommand command = assertInstanceOf(
                ListCommand.class, Command.parse("list unexpected"));

        assertThrows(ZuccException.class, command::requireNoArgument);
    }

    @Test
    public void isExit_exitAndRegularCommands_returnsExpectedValues() throws ZuccException {
        assertTrue(Command.parse("bye").isExit());
        assertFalse(Command.parse("list").isExit());
    }
}
