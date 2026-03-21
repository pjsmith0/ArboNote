package com.pjs.ui.htmleditor.toolbar.command;

import com.pjs.ui.htmleditor.toolbar.command.action.StrikethroughAction;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Optional;

public class StrikethroughCommand implements Command {

    @Override
    public Optional<Action> getAction() {
        return Optional.of(new StrikethroughAction());
    }

    @Override
    public String getActionMapKey() {
        return "font-strike";
    }

    @Override
    public String getText() {
        return "S";
    }

    @Override
    public String getTooltipText() {
        return "Strikethrough";
    }

    @Override
    public ImageIcon getIcon() {
        return generateIcon("/icons/type-strikethrough.png");
    }

    @Override
    public Optional<Integer> getKeyEvent() {
        return Optional.of(KeyEvent.VK_S);
    }
}
