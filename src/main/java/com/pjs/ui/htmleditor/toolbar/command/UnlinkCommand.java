package com.pjs.ui.htmleditor.toolbar.command;

import com.pjs.ui.htmleditor.toolbar.command.action.UnlinkAction;

import javax.swing.*;
import java.util.Optional;

public class UnlinkCommand implements Command {

    @Override
    public Optional<Action> getAction() {
        return Optional.of(new UnlinkAction());
    }

    @Override
    public String getActionMapKey() {
        return "unlink";
    }

    @Override
    public String getText() {
        return "Unlink";
    }

    @Override
    public String getTooltipText() {
        return "Unlink";
    }

    @Override
    public ImageIcon getIcon() {
        return generateIcon("/icons/link-45deg.png");
    }
}
