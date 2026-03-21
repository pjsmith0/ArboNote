package com.pjs.ui.htmleditor.toolbar.command;

import com.pjs.ui.htmleditor.toolbar.command.action.LinkAction;

import javax.swing.*;
import java.util.Optional;

public class LinkCommand implements Command {

    @Override
    public Optional<Action> getAction() {
        return Optional.of(new LinkAction());
    }

    @Override
    public String getActionMapKey() {
        return "link";
    }

    @Override
    public String getText() {
        return "Link";
    }

    @Override
    public String getTooltipText() {
        return "Link";
    }

    @Override
    public ImageIcon getIcon() {
        return generateIcon("/icons/link.png");
    }

}
