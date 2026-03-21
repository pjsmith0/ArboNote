package com.pjs.ui.htmleditor.toolbar.command;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Optional;

public class UnderlineCommand implements Command {

    @Override
    public String getActionMapKey() {
        return "font-underline";
    }

    @Override
    public String getText() {
        return "U";
    }

    @Override
    public String getTooltipText() {
        return "Underline";
    }

    @Override
    public ImageIcon getIcon() {
        return generateIcon("/icons/type-underline.png");
    }

    @Override
    public Optional<Integer> getKeyEvent() {
        return Optional.of(KeyEvent.VK_U);
    }
}
