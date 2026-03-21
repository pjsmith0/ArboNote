package com.pjs.ui.htmleditor.toolbar.command;

import com.pjs.ui.htmleditor.toolbar.command.action.IndentAction;

import javax.swing.*;
import java.util.Optional;

public class ReduceIndentCommand implements Command {

    @Override
    public Optional<Action> getAction() {
        return Optional.of(new IndentAction("reduce-indent", -20f));
    }

    @Override
    public String getActionMapKey() {
        return "reduce-indent";
    }

    @Override
    public String getText() {
        return "-Indent";
    }

    @Override
    public String getTooltipText() {
        return "Reduce Indent";
    }

    @Override
    public ImageIcon getIcon() {
        return generateIcon("/icons/unindent.png");
    }

}
