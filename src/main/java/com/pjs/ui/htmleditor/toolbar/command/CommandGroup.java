package com.pjs.ui.htmleditor.toolbar.command;

import java.util.Arrays;
import java.util.List;

public class CommandGroup {

    private final List<Command> commands;

    public CommandGroup(Command... commands) {
        this.commands = Arrays.asList(commands);
    }

    public List<Command> getCommands() {
        return commands;
    }
}
