package com.pjs.ui.htmleditor.toolbar.command;

import javax.swing.*;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Optional;

public class RedoCommand implements Command {

    private final UndoManager undoManager;

    public RedoCommand(UndoManager undoManager) {
        this.undoManager = undoManager;
    }

    @Override
    public Optional<Action> getAction() {
        return Optional.of(new AbstractAction() {

            private static final long serialVersionUID = 42L;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    if (undoManager.canRedo()) {
                        undoManager.redo();
                    }
                } catch (CannotUndoException ex) {
                    throw new CommandException(ex);
                }
            }
        });
    }

    @Override
    public String getActionMapKey() {
        return "redo";
    }

    @Override
    public String getText() {
        return "Redo";
    }

    @Override
    public String getTooltipText() {
        return "Redo";
    }

    @Override
    public ImageIcon getIcon() {
        return generateIcon("/icons/arrow-clockwise.png");
    }

    @Override
    public Optional<Integer> getKeyEvent() {
        return Optional.of(KeyEvent.VK_Y);
    }
}
