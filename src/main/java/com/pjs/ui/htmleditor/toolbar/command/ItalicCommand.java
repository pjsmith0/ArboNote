package com.pjs.ui.htmleditor.toolbar.command;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Optional;

public class ItalicCommand implements Command {

    @Override
    public String getActionMapKey() {
        return "font-italic";
    }

    @Override
    public String getText() {
        return "I";
    }

    @Override
    public String getTooltipText() {
        return "Italic";
    }

    @Override
    public ImageIcon getIcon() {
        return generateIcon("/icons/type-italic.png");
    }

    @Override
    public Optional<Integer> getKeyEvent() {
        return Optional.of(KeyEvent.VK_I);
    }
}
