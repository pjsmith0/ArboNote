package com.pjs.ui.htmleditor.toolbar.command;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Optional;

public class BoldCommand implements Command {

    @Override
    public String getActionMapKey() {
        return "font-bold";
    }

    @Override
    public String getText() {
        return "B";
    }

    @Override
    public String getTooltipText() {
        return "Bold";
    }

    @Override
    public ImageIcon getIcon() {
        return generateIcon("/icons/type-bold.png");
    }

    @Override
    public Optional<Integer> getKeyEvent() {
        return Optional.of(KeyEvent.VK_B);
    }
}
