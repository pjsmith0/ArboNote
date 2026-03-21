package com.pjs.ui.htmleditor.toolbar.command;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Optional;

public interface Command {

    default Optional<ActionListener> getActionListener() {
        return Optional.empty();
    }

    default Optional<Action> getAction() {
        return Optional.empty();
    }

    String getActionMapKey();

    String getText();

    default boolean isRequestFocusEnabled() {
        return false;
    }

    String getTooltipText();

    ImageIcon getIcon();

    default Optional<Integer> getKeyEvent() {
        return Optional.empty();
    }

    default ImageIcon generateIcon(String iconLocation) {
        URL resource = getClass().getResource(iconLocation);
        return new ImageIcon(resource);
    }

}
