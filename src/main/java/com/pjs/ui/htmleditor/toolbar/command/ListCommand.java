package com.pjs.ui.htmleditor.toolbar.command;

import com.pjs.ui.htmleditor.toolbar.command.action.ListAction;

import javax.swing.*;
import java.util.Optional;

public class ListCommand implements Command {

    @Override
    public Optional<Action> getAction() {
        return Optional.of(new ListAction());
    }

    @Override
    public String getActionMapKey() {
        return "insert-unordered-list";
    }

    @Override
    public String getText() {
        return "Unorder List";
    }

    @Override
    public String getTooltipText() {
        return "Unorder List";
    }

    @Override
    public ImageIcon getIcon() {
        return generateIcon("/icons/list-ul.png");
    }
}
