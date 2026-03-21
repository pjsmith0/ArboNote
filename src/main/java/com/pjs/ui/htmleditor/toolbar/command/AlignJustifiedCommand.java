package com.pjs.ui.htmleditor.toolbar.command;

import javax.swing.*;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import java.util.Optional;

public class AlignJustifiedCommand implements Command {

    @Override
    public Optional<Action> getAction() {
        return Optional.of(new StyledEditorKit.AlignmentAction("Justify", StyleConstants.ALIGN_JUSTIFIED));
    }

    @Override
    public String getActionMapKey() {
        return "justified";
    }

    @Override
    public String getText() {
        return "Left";
    }

    @Override
    public String getTooltipText() {
        return "Align Justified";
    }

    @Override
    public ImageIcon getIcon() {
        return generateIcon("/icons/align-middle.png");
    }

}
