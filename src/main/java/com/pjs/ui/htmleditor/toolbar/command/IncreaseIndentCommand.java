package com.pjs.ui.htmleditor.toolbar.command;

import com.pjs.ui.htmleditor.toolbar.command.action.IndentAction;

import javax.swing.*;
import java.util.Optional;

public class IncreaseIndentCommand implements Command {

    @Override
    public Optional<Action> getAction() {
        return Optional.of(new IndentAction(getActionMapKey(), 20f));
    }

    @Override
    public String getActionMapKey() {
        return "increment-indent";
    }

    @Override
    public String getText() {
        return "+Indent";
    }

    @Override
    public String getTooltipText() {
        return "Increase Indent";
    }

    @Override
    public ImageIcon getIcon() {
        return generateIcon("/icons/indent.png");
    }

}
